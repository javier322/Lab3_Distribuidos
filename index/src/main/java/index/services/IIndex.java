package index.services;


import index.model.Title;
import java.io.IOException;
import java.util.ArrayList;

public interface IIndex {

    public void indexar();
     public ArrayList<Title> search(String title, int n);
    // public void deleteDocument(Title vol) throws IOException;
    // public void addDocument(Title vol) throws IOException;
    // public void updateIndex(Title vol) throws IOException;
    // public ArrayList<String> getFieldTerms(String field) throws  IOException;
}
