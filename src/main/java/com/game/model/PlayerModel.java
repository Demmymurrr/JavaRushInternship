package com.game.model;

import com.game.controller.PlayerOrder;
import com.game.entity.Player;
import com.game.entity.Profession;
import com.game.entity.Race;
import com.game.exception.BadRequestException;
import com.game.exception.NotFoundException;
import com.game.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PlayerModel {

    private final PlayerRepository playerRepository;

    @Autowired
    public PlayerModel(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }


    public List<Player> getPlayersWithParam(String name, String title, String race, String profession,
                                            String afterDate, String beforeDate, String banned,
                                            String minExperience, String maxExperience, String minLevel,
                                            String maxLevel) {
        List<Player> allPlayers = playerRepository.findAll();
        //отсеиваем по banned:
        if (banned != null) {
            boolean isBanned = Boolean.parseBoolean(banned);
            allPlayers = allPlayers.stream().filter(player -> player.isBanned() == isBanned).collect(Collectors.toList());
        }
        //отсеиваем по race
        if (race != null) {
            Race parsedRace = Race.valueOf(race);
            allPlayers = allPlayers.stream().filter(player -> player.getRace() == parsedRace).collect(Collectors.toList());
        }
        //отсеиваем по profession
        if (profession != null) {
            Profession parsedProfession = Profession.valueOf(profession);
            allPlayers = allPlayers.stream().filter(player -> player.getProfession() == parsedProfession).collect(Collectors.toList());
        }
        //отсеиваем по lvl
        if (minLevel != null || maxLevel != null) {
            int minLvl = minLevel == null ? 0 : Integer.parseInt(minLevel);
            int maxLvl = maxLevel == null ? Integer.MAX_VALUE : Integer.parseInt(maxLevel);
            allPlayers = allPlayers.stream().filter(player -> (player.getLevel() >= minLvl && player.getLevel() <= maxLvl)).collect(Collectors.toList());
        }
        //отсеиваем по Exp

        if (minExperience != null || maxExperience != null) {
            int minExp = minExperience == null ? 0 : Integer.parseInt(minExperience);
            int maxExp = maxExperience == null ? 10_000_000 : Integer.parseInt(maxExperience);
            allPlayers = allPlayers.stream().filter(player -> (player.getExperience() >= minExp && player.getExperience() <= maxExp)).collect(Collectors.toList());
        }
        //отсеиваем по Date
        if (afterDate != null) {
            Date after = new Date(Long.parseLong(afterDate));
            allPlayers = allPlayers.stream().filter(player -> player.getBirthday().after(after)).collect(Collectors.toList());
        }
        if (beforeDate != null) {
            Date before = new Date(Long.parseLong(beforeDate));
            allPlayers = allPlayers.stream().filter(player -> player.getBirthday().before(before)).collect(Collectors.toList());
        }
        //отсеиваем по name
        if (name != null) {
            allPlayers = allPlayers.stream().filter(player -> player.getName().
                    toLowerCase().contains(name.toLowerCase())).collect(Collectors.toList());
        }
        //отсеиваем по title
        if (title != null) {
            allPlayers = allPlayers.stream().filter(player -> player.getTitle().
                    toLowerCase().contains(title.toLowerCase())).collect(Collectors.toList());
        }

        return allPlayers;
    }

    public int allCount(String name, String title, String race, String profession,
                        String afterDate, String beforeDate, String banned,
                        String minExperience, String maxExperience, String minLevel,
                        String maxLevel) {
        return getPlayersWithParam(name, title, race, profession, afterDate, beforeDate, banned,
                minExperience, maxExperience, minLevel, maxLevel).size();
    }

    public List<Player> viewer(List<Player> players, String pageNumber, String pageSize, String playerOrder) {
        int pageN = 0;
        if (pageNumber != null)
            pageN = Integer.parseInt(pageNumber);

        int pageS = 3;
        if (pageSize != null)
            pageS = Integer.parseInt(pageSize);

        PlayerOrder order = PlayerOrder.ID;
        if (playerOrder != null)
            order = PlayerOrder.valueOf(playerOrder);

        PlayerOrder finalOrder = order;
        Comparator<Player> comparator = new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                switch (finalOrder) {
                    case ID:
                        return o1.getId() - o2.getId() > 0 ? 1 :
                                o1.getId() - o2.getId() < 0 ? -1 : 0;
                    case NAME:
                        return o1.getName().compareTo(o2.getName());
                    case EXPERIENCE:
                        return o1.getExperience() - o2.getExperience();
                    case BIRTHDAY:
                        return o1.getBirthday().compareTo(o2.getBirthday());
                }
                return 0;
            }
        };

        players.sort(comparator);
        List<Player> result;
        int initSublist = pageS * pageN;
        int closeSublist = Math.min(pageS * (pageN + 1), players.size());
        result = players.subList(initSublist, closeSublist);
        return result;
    }

    public Player getOne(String id) {
        long parseId=getValidId(id);
        return playerRepository.findAll().stream().filter(player -> player.getId() == parseId).findAny().orElseThrow(NotFoundException::new);
    }

    public Player createOne(Map<String ,String> request) {
        Player player = creationWithValidation(request);
        playerRepository.save(player);
        return player;
    }

    //TODO:
    public Player update(String id, Map<String,String> request) {
        long parseId=getValidId(id);
        Player player = playerRepository.findAll().stream().filter(player1 -> player1.getId() == parseId).findAny().orElseThrow(NotFoundException::new);
        //создание request для метода creationWithValidation
        //begin
        String name = request.get("name");
        if (name == null) {request.put("name", player.getName());}

        String title = request.get("title");
        if (title == null) {request.put("title",player.getTitle());}

        String race = request.get("race");
        if (race == null) {request.put("race",player.getRace().toString());}

        String profession = request.get("profession");
        if (profession == null ) {request.put("profession",player.getProfession().toString());}

        String birthday = request.get("birthday");
        if (birthday == null) {
            request.put("birthday", ( (Long) player.getBirthday().getTime()).toString());
        }

        String banned = request.get("banned");
        if (banned == null) {request.put("banned",((Boolean) player.isBanned()).toString());}

        String experience = request.get("experience");
        if (experience == null) {
            request.put("experience",((Integer) player.getExperience()).toString());
        }
        //end
        player = creationWithValidation(request);
        player.setId(parseId);
        playerRepository.save(player);
        return player;
    }

    public void delete(String id) {
        long parseId=getValidId(id);
        playerRepository.findAll().stream().filter(player1 -> player1.getId() == parseId).findAny().orElseThrow(NotFoundException::new);
        playerRepository.deleteById(parseId);
    }


    private Player creationWithValidation(Map<String,String> request) {
        String name = request.get("name");
        String title = request.get("title");
        String race = request.get("race");
        String profession = request.get("profession");
        String birthday = request.get("birthday");
        String experience = request.get("experience");
        String banned = request.get("banned");
        // проверка на нули
        if (name != null && title != null && race != null && profession != null && birthday != null && experience != null) {
            Player player = new Player();
            // проверка длины имени 12 (не пустая строка)
            if (name.length()<=12 && !name.trim().equals("")) {
                player.setName(name);
            } else throw new BadRequestException(); //except
            // проверка длины тайтла 30
            if (title.length()<=30) {
                player.setTitle(title);
            } else throw new BadRequestException(); //except
            // опыт 0..10_000_000
            int exp = Integer.parseInt(experience);
            if (exp>=0 && exp<=10_000_000) {
                player.setExperience(exp);
                int lvl =getLvl(exp);
                player.setLevel(lvl);
                int untilNextLvl = getUntilNextLvl(lvl,exp);
                player.setUntilNextLevel(untilNextLvl);
            } else throw new BadRequestException(); //except
            // birthday > [Long] 0 and [2000..3000y]
            long brthday = Long.parseLong(birthday);
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(brthday);
            Calendar afterBorder = new GregorianCalendar(2000,0,0);
            Calendar beforeBorder = new GregorianCalendar(3000,0,0);
            if (brthday > 0 && cal.after(afterBorder) && cal.before(beforeBorder)) {
                player.setBirthday(cal.getTime());
            } else throw new BadRequestException(); //except
            // banned
            if (banned!=null) {
                boolean isBanned = Boolean.parseBoolean(banned);
                player.setBanned(isBanned);
            }
            Race playerRace = Race.valueOf(race);
            player.setRace(playerRace);
            Profession playerProfession = Profession.valueOf(profession);
            player.setProfession(playerProfession);
            return player;
        } else throw new BadRequestException();
    }

    private int getLvl(int exp) {
        return (int) (Math.sqrt(2500+200*exp)-50)/100;
    }

    private int getUntilNextLvl(int lvl, int exp) {
        return 50*(lvl+1)*(lvl+2)-exp;
    }

    private long getValidId(String id) {
        long parseId;
        try {
            parseId = Long.parseLong(id);
        } catch (Exception e) {
            throw new BadRequestException();
        }
        if (parseId<=0) throw new BadRequestException();
        return parseId;
    }

}
