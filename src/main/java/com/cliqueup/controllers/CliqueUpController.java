package com.cliqueup.controllers;

import com.cliqueup.entities.*;
import com.cliqueup.services.*;
import com.cliqueup.utlities.PasswordStorage;
import okhttp3.*;
import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by michaelplott on 11/16/16.
 */
@RestController
public class CliqueUpController {

    //public static final String REDIRECTURL = "http://10.1.10.44:8080/access";

    public static final String REDIRECTURL = "http://127.0.0.1:8080/access";

    public static final String AUTHORIZE_URL = "https://secure.meetup.com/oauth2/authorize";

    public static final String CLIENT_ID = "dlevog3jrgb2rn4rr30hv6rs5b";

    public static final String SECRET = "g3dd8kblqshkq7jrplj79et6ko";

    public static final String AUTH = AUTHORIZE_URL+"?client_id="+CLIENT_ID+"&response_type=code&redirect_uri="+REDIRECTURL;

    @Autowired
    UserRepo users;

    @Autowired
    GroupRepo groups;

    @Autowired
    DirectMessageRepo dms;

    @Autowired
    ChatMessageRepo cms;

    @Autowired
    TokenRepo tokens;

    @Autowired
    FriendRepo friends;

    Server h2;

    @PostConstruct
    public void init() throws SQLException, ParseException, PasswordStorage.CannotPerformOperationException {
        h2.createWebServer().start();

        if (users.count() == 0) {
            users.save(new User(12324143, "http://statici.behindthevoiceactors.com/behindthevoiceactors/_img/chars/mikey-blumberg-disneys-recess-9.77.jpg",
                    "mike",
                    false,
                    PasswordStorage.createHash("123")));
            users.save(new User(123412,
                    "profilepics/sloth.jpg",
                    "sam",
                    true,
                    PasswordStorage.createHash("123")));
            users.save(new User(123412,
                    "profilepics/mrfreeze.jpg",
                    "rob",
                    true,
                    PasswordStorage.createHash("123")));
            users.save(new User(12312,
                    "http://facebookcraze.com/wp-content/uploads/2010/10/fake-facebook-profile-picture-funny-batman-pic.jpg ",
                    "Henry",
                    true,
                    PasswordStorage.createHash("123")));
            users.save(new User(216728604,
                    "victor123",
                    false,
                    PasswordStorage.createHash("123")));
            users.save(new User(159325052,
                    "mikeymike",
                    false,
                    PasswordStorage.createHash("mikeymike")));
        }

        if (groups.count() == 0) {
            User user = users.findByUsername("Henry");
            User coOrganizer = users.findByUsername("mikeymike");
            User adminUser = users.findByUsername("victor123");
            groups.save(new Group("Beer-Enthusiasts", "Beer-Enthusiasts Group", user));
            groups.save(new Group("CliqueUp Dev Team",
                                  "Group for the CliqueUp Development team",
                                  coOrganizer.getUsername(),
                                  adminUser));
        }

        if (dms.count() == 0) {
            User user = users.findByUsername("mike");
            User recipient = users.findByUsername("Henry");
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date date = dateFormat.parse("11/16/2016");
            long time = date.getTime();
            dms.save(new DirectMessage("Hey what time is the event again?", recipient.getUsername(), user));
        }

        if (cms.count() == 0) {
            User user = users.findByUsername("mike");
            Group group = groups.findOne(1);
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date date = dateFormat.parse("11/16/2016");
            long time = date.getTime();
            cms.save(new ChatMessage("Hey what time is the meetup? I can't get ahold of Henry", group, user));
        }

        if (friends.count() == 0) {
            User user = users.findByUsername("mike");
            Friend friend = new Friend("Henry",
                    "http://facebookcraze.com/wp-content/uploads/2010/10/fake-facebook-profile-picture-funny-batman-pic.jpg",
                    user);
            Friend friend1 = new Friend("sam",
                    "http://facebookcraze.com/wp-content/uploads/2010/10/fake-facebook-profile-picture-funny-batman-pic.jpg",
                    user);
            Friend friend2 = new Friend("rob",
                    "http://facebookcraze.com/wp-content/uploads/2010/10/fake-facebook-profile-picture-funny-batman-pic.jpg",
                    user);
            friends.save(friend);
            friends.save(friend1);
            friends.save(friend2);
        }
    }

    @PreDestroy
    public void destroy() {
        h2.stop();
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public User userAuth(HttpSession session, @RequestBody User user) throws Exception {
        User userFromDb = users.findByUsername(user.getUsername());
        if (userFromDb == null) {
            user.setPassword(PasswordStorage.createHash(user.getPassword()));
            User userForDb = new User(user.getUsername(), true, user.getPassword());
            users.save(userForDb);
            session.setAttribute("username", userForDb.getUsername());
            return userFromDb;
        }
        else if (!PasswordStorage.verifyPassword(user.getPassword(), userFromDb.getPassword())) {
            throw new Exception("Password invalid");
        }
        session.setAttribute("username", user.getUsername());
        userFromDb.setOnline(!userFromDb.isOnline());
        users.save(userFromDb);
        return userFromDb;
    }

    @RequestMapping(path = "/auth", method = RequestMethod.GET)
    public void getAuth(HttpSession session,HttpServletResponse response) throws IOException, ServletException {
        String username = (String) session.getAttribute("username");
        String url = AUTH + "?username=" + username;
        response.sendRedirect(url);
    }

    @RequestMapping(path = "/access", method = RequestMethod.GET)
    public void getAccess(String code, String username , HttpServletResponse myResponse, HttpSession session) throws IOException {
        User user = users.findByUsername(username);
        OkHttpClient client = new OkHttpClient();
        okhttp3.RequestBody formBody = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", SECRET)
                .add("grant_type", "authorization_code")
                .add("redirect_uri", REDIRECTURL + "?username=" + username)
                .add("code", code)
                .build();
        Request myRequest = new Request.Builder()
                .url("https://secure.meetup.com/oauth2/access")
                .post(formBody)
                .build();
        okhttp3.Response response = client.newCall(myRequest).execute();
        Token token = new Token(response.body().string());
        tokens.save(token);
        session.setAttribute("token", token.getId());
        user.setToken(token);
        users.save(user);
        session.setAttribute("username", user.getUsername());
        if (!response.isSuccessful())
            throw new IOException("CliqueUp server error: " + response);
        myResponse.sendRedirect("/#/homePage");
    }

    @RequestMapping(path = "/gettoken", method = RequestMethod.GET)
    public Token getToken(HttpSession session) throws Exception {
        String username = (String) session.getAttribute("username");
        int id = (int) session.getAttribute("token");
        if (username == null) {
            throw new Exception("Not logged in");
        }
        return tokens.findOne(id);
    }

    @RequestMapping(path = "/user", method = RequestMethod.GET)
    public ResponseEntity<User> getUser(HttpSession session) {
        String username = (String) session.getAttribute("username");
        User user = users.findByUsername(username);
        return new ResponseEntity<User>(user, HttpStatus.OK);
    }

    @RequestMapping(path = "/logout", method = RequestMethod.POST)
    public void logout(HttpSession session, HttpServletResponse response) throws Exception {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new Exception("Not logged in!");
        }
        User userFromDb = users.findByUsername(username);
        if (userFromDb == null) {
            throw new Exception("User does not exist!");
        }
        int id = userFromDb.getToken().getId();
        userFromDb.setToken(null);
        tokens.delete(id);
        userFromDb.setOnline(false);
        users.save(userFromDb);
        session.invalidate();
    }

    @RequestMapping(path = "/friends", method = RequestMethod.GET)
    public ResponseEntity<HashMap<String, ArrayList>> getOnlineUsers(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<HashMap<String, ArrayList>>(HttpStatus.FORBIDDEN);
        }
        HashMap<String, ArrayList> json = new HashMap<>();
        User user = users.findByUsername(username);
        ArrayList<User> onlineUsers = new ArrayList<>();
        ArrayList<Friend> userFriends = friends.findAllByUser(user);
        ArrayList<User> allUsers = users.findAll();
        for (User user1 : allUsers) {
            if (user1.isOnline()) {
                onlineUsers.add(user1);
            }
        }
        json.put("onlineUsers", onlineUsers);
        json.put("userFriends", userFriends);
        return new ResponseEntity<HashMap<String, ArrayList>>(json, HttpStatus.OK);
    }

    @RequestMapping(path = "/friends", method = RequestMethod.POST)
    public ResponseEntity<ArrayList<Friend>> addFriends(HttpSession session, String friendName) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return new ResponseEntity<ArrayList<Friend>>(HttpStatus.FORBIDDEN);
        }
        User user = users.findByUsername(username);
        User userFriend = users.findByUsername(friendName);
        friends.save(new Friend(userFriend.getUsername(), userFriend.getImage(), userFriend.getMeetupId(), user));
        return new ResponseEntity<ArrayList<Friend>>(friends.findAllByUser(user), HttpStatus.OK);
    }

    @RequestMapping(path = "/image", method = RequestMethod.POST)
    public String saveImage(HttpSession session, String photo, Integer meetupId) {
        String username = (String) session.getAttribute("username");
        User user = users.findByUsername(username);
        user.setImage(photo);
        user.setMeetupId(meetupId);
        users.save(user);
        return user.getImage();
    }

    @RequestMapping(path = "/chat", method = RequestMethod.GET)
    public ArrayList<ChatMessage> getSpecificMessges(HttpSession session, String groupname) {
        String username = (String) session.getAttribute("username");
        User user = users.findByUsername(username);
        Group group = groups.findByName(groupname);
        return cms.findByGroup(group);
    }

    @RequestMapping(path = "/signup", method = RequestMethod.POST)
    public ResponseEntity<User> userSignUp(HttpSession session, @RequestBody User user) throws PasswordStorage.CannotPerformOperationException {
        if (user.getUsername() == null || user.getPassword() == null) {
            return new ResponseEntity<User>(HttpStatus.EXPECTATION_FAILED);
        }
        if (user.getImage() == null) {
            User userForDb = new User(null, user.getUsername(), true, PasswordStorage.createHash(user.getPassword()));
        }
        User userForDb = new User(user.getImage(), user.getUsername(), true, PasswordStorage.createHash(user.getPassword()));
        users.save(userForDb);
        session.setAttribute("username", userForDb.getUsername());
        return new ResponseEntity<User>(userForDb, HttpStatus.OK);
    }
}