package index.services;


import index.model.Title;
import java.io.IOException;
import java.util.ArrayList;

public interface IIndex {

    public void indexar();
     public ArrayList<Title> search(String title, int n);
   
}
