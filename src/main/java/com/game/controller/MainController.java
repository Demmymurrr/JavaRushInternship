package com.game.controller;


import com.game.entity.Player;
import com.game.model.PlayerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("rest/players")
public class MainController {

    private final PlayerModel playerModel;

    @Autowired
    public MainController(PlayerModel playerModel) {
        this.playerModel = playerModel;
    }


   @GetMapping
   public List<Player> getPlayersWithParam (@RequestParam(value = "name", required = false) String name,
                                            @RequestParam(value = "title", required = false) String title,
                                            @RequestParam(value = "race", required = false) String race,
                                            @RequestParam(value = "profession", required = false) String profession,
                                            @RequestParam(value = "after", required = false) String afterDate,
                                            @RequestParam(value = "before", required = false) String beforeDate,
                                            @RequestParam(value = "banned", required = false) String banned,
                                            @RequestParam(value = "minExperience", required = false) String minExperience,
                                            @RequestParam(value = "maxExperience", required = false) String maxExperience,
                                            @RequestParam(value = "minLevel", required = false) String minLevel,
                                            @RequestParam(value = "maxLevel", required = false) String maxLevel,
                                            @RequestParam(value = "pageNumber", required = false) String pageNumber,
                                            @RequestParam(value = "pageSize", required = false) String pageSize,
                                            @RequestParam(value = "order", required = false) String order) {
        List<Player> players = playerModel.getPlayersWithParam(name,title,race,profession,
                afterDate,beforeDate,banned,minExperience, maxExperience,
                minLevel,maxLevel);
       return playerModel.viewer(players,pageNumber,pageSize,order);
   }

    @GetMapping("count")
    public int allCount (@RequestParam(value = "name", required = false) String name,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "race", required = false) String race,
                         @RequestParam(value = "profession", required = false) String profession,
                         @RequestParam(value = "after", required = false) String afterDate,
                         @RequestParam(value = "before", required = false) String beforeDate,
                         @RequestParam(value = "banned", required = false) String banned,
                         @RequestParam(value = "minExperience", required = false) String minExperience,
                         @RequestParam(value = "maxExperience", required = false) String maxExperience,
                         @RequestParam(value = "minLevel", required = false) String minLevel,
                         @RequestParam(value = "maxLevel", required = false) String maxLevel) {
        return playerModel.allCount(name,title,race,profession,afterDate,beforeDate,banned,
                minExperience,maxExperience,minLevel,maxLevel);
    }


    @GetMapping("{id}")
    public Player getOne (@PathVariable("id") String id) {
        return playerModel.getOne(id);
    }

    @PostMapping
    public Player create(@RequestBody Map<String , String> request) {
        return playerModel.createOne(request);
    }

    @PostMapping("{id}")
    public Player update (@PathVariable("id") String id,
                          @RequestBody Map<String,String> request) {
        return playerModel.update(id,request);
    }

    @DeleteMapping("{id}")
    public void delete (@PathVariable("id") String id) {
        playerModel.delete(id);
    }
}
