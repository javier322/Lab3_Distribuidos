package index.controllers;

import index.model.Title;
import index.services.IIndex;


import java.util.ArrayList;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import org.redisson.Redisson;

import org.redisson.config.Config;
import org.redisson.api.RList;

import org.redisson.api.RedissonClient;
import org.redisson.codec.*;



@CrossOrigin
@RestController
@RequestMapping("/index")
public class indexService {

    @Autowired
    private IIndex index;

    // @RequestMapping(value = "/create", method = RequestMethod.GET)
    // @ResponseBody
    // public HttpStatus createIndex() {
    //     System.out.println("INDEX");
    //     index.indexar();
    //     return HttpStatus.OK;
    // }

   
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public Iterable<Title> searchVoluntaries(@RequestBody HashMap<String, Object> json) {

        int limit = (int) json.get("limit");
        String title = (String) json.get("title");
        Config config = new Config();

        config.useSingleServer().setAddress("redis://localhost:6379");

        FstCodec codec = new FstCodec();
        config.setCodec(codec);

        RedissonClient client = Redisson.create(config);

        String num= Integer.toString(limit);
        RList<Title> list = client.getList(title+num);

        if (list.isExists()) {
            System.out.println("cache");
            return list;
        }

        ArrayList<Title> titles = new ArrayList<Title>();


        titles = index.search(title, limit);
        for (Title tl : titles) {

            list.add(tl);

        }

        client.shutdown();
        return titles;
    }


}
