package index.services;

import index.model.Title;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.nio.charset.StandardCharsets;

import com.google.api.gax.paging.Page;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ReadChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.contrib.nio.CloudStorageConfiguration;
import com.google.cloud.storage.contrib.nio.CloudStorageFileSystem;

import javax.print.Doc;

import java.util.*;

import static java.lang.Integer.parseInt;

@Service
public class indexLucene implements IIndex {

    private static final String SAMPLE_CSV_FILE_PATH = "./titles.csv";

    // esto se debe modificar ya que no deberi estar tan acoplado a la bd
    public void indexar() {
        try {
            System.out.println("INSIDE BUILT");
            Directory dir = FSDirectory.open(Paths.get("/var/index/"));
            System.out.println("1");
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            System.out.println("2");

            // Create a new index in the directory, removing any
            // previously indexed documents:
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            //// Add new documents to an existing index: OpenMode.CREATE_OR_APPEND
            System.out.println("3");
            IndexWriter writer = new IndexWriter(dir, iwc);
            System.out.println("4");

            addDocumentCsv(writer);
            System.out.println("cLOSE");
            writer.close();
        } catch (IOException ioe) {

            System.out.println("FAIL");

        }
    }

    public ArrayList<Title> search(String title, int n) {

        ArrayList<Title> titles = new ArrayList<Title>();

        System.out.println("query: " + title);

        try {



            // String PATH = "./index1/";

            // File directory = new File(PATH);  
            // System.out.println("0kikko");

            // if (!directory.exists()) {
            //     System.out.println("asno");
            //     directory.mkdir();
            //     // If you require it to make the entire directory path including parents,
            //     // use directory.mkdirs(); here instead.
            // }
            // System.out.println("asino");


            // for (Blob blob : blobs.iterateAll()) {
            //     blob.downloadTo(Paths.get("./index1/"));
            //     System.out.println("kasjjsisjisjsijs");
            // }
            // System.out.println("asincrono");
            // CloudStorageFileSystem fs =
            // CloudStorageFileSystem.forBucket("titles_repository",
            // CloudStorageConfiguration.DEFAULT, storage);

            // Path path=fs.getPath("index1/");
            // System.out.println("ajsjsjs");
            // Path path=Paths.get(URI.create("gs://titles_repository/index1"));
            // se debe desacoplar esto
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("/var/index/")));
            IndexSearcher searcher = new IndexSearcher(reader);

            Analyzer analyzer = new StandardAnalyzer();

            QueryParser parser = new QueryParser("originalTitle", analyzer);
            Query query = parser.parse(title);

            TopDocs results = searcher.search(query, n);
            ScoreDoc[] hits = results.scoreDocs;

            System.out.println(hits.length);
            for (int i = 0; i < hits.length; i++) {
                // List<String> genres = new ArrayList<String>();
                // List<IndexableField> specificScore = new ArrayList<IndexableField>();
                // List<IndexableField> genericDimName = new ArrayList<IndexableField>();
                // List<IndexableField> genericDimScore = new ArrayList<IndexableField>();

                // ArrayList<HashMap<String, Object>> general_dimensions = new
                // ArrayList<HashMap<String, Object>>();
                // ArrayList<HashMap<String, Object>> requirements = new
                // ArrayList<HashMap<String, Object>>();
                Document doc = searcher.doc(hits[i].doc);
                System.out.println(hits[i].shardIndex + " " + hits[i].score);

                // String id_salida = doc.get("id");
                System.out.println(doc.get("originalTitle"));
                Title tl = new Title();
                tl.setTitleType(doc.get("titleType"));
                tl.setPrimaryTitle(doc.get("primaryTitle"));
                tl.setOriginalTitle(doc.get("originalTitle"));
                tl.setStartYear(doc.get("startYear"));
                tl.setEndYear(doc.get("endYear"));
                ArrayList<String> genres = new ArrayList<String>(Arrays.asList(doc.get("genres").split(" ")));
                tl.setGenres(genres);

                // specificScore = Arrays.asList(doc.getFields("specificScore"));
                // genericDimName = Arrays.asList(doc.getFields("genericDimName"));
                // genericDimScore = Arrays.asList(doc.getFields("genericDimScore"));

                System.out.println(genres.size());

                titles.add(tl);
            }
            reader.close();

        } catch (IOException ioe) {

        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("Datos encontrado para esta consulta" + titles.size());

        return titles;

    }

    public void addDocumentCsv(IndexWriter writer) throws IOException {
        System.out.println("INSIDE CSV");

        Credentials credentials = GoogleCredentials
                .fromStream(new FileInputStream("./credentials.json"));
        System.out.println("aqui me caigo");
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId("steady-tracer-243022")
                .build().getService();

        // Get specific file from specified bucket
        Blob blob = storage.get(BlobId.of("titles_repository", "titles.csv"));

        ReadChannel readChannel = blob.reader();
        // BufferedReader reader =
        // Files.newBufferedReader(Paths.get(SAMPLE_CSV_FILE_PATH),
        // StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(Channels.newReader(readChannel, "UTF-8"));

        System.out.println("rEAD1");
        CSVReader csvReader = new CSVReader(reader);
        System.out.println("rEAD");

        String[] nextRecord;
        while ((nextRecord = csvReader.readNext()) != null) {

            Document doc = null;
            doc = new Document();

            doc.add(new StringField("titleId", nextRecord[0], Field.Store.YES));
            doc.add(new TextField("titleType", nextRecord[1], Field.Store.YES));
            doc.add(new TextField("primaryTitle", nextRecord[2], Field.Store.YES));
            doc.add(new TextField("originalTitle", nextRecord[3], Field.Store.YES));
            doc.add(new StringField("startYear", nextRecord[5], Field.Store.YES));
            doc.add(new StringField("endYear", nextRecord[6], Field.Store.YES));
            if (nextRecord.length < 9) {

                System.out.println("Aqui");
                doc.add(new TextField("genres", "N", Field.Store.YES));
            } else {
                doc.add(new TextField("genres", nextRecord[8].replace(',', ' '), Field.Store.YES));
            }

            // System.out.println("titleId : " + nextRecord[0]);
            // System.out.println("titleType : " + nextRecord[1]);
            // System.out.println("PrimaryTitle : " + nextRecord[2]);
            // System.out.println("originalTitle : " + nextRecord[3]);
            // System.out.println("startYear : " + nextRecord[5]);
            // System.out.println("endYear : " + nextRecord[6]);
            // System.out.println("genres : " + nextRecord[8].replace(',', ' '));
            // System.out.println("==========================");

            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE_OR_APPEND) {
                // System.out.println("Indexando el archivo: " + elemento.get("_id") + "con
                // texto" + elemento.get("text"));
                writer.addDocument(doc);
            }
        }
        csvReader.close();
        System.out.println("eND CSV");

    }

}
