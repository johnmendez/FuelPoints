package org.FuelPoints.controllers;

import org.FuelPoints.utilities.parsers.RootParser;
import org.FuelPoints.utilities.serializers.JsonDataSerializer;
import org.FuelPoints.utilities.serializers.RootSerializer;
import org.FuelPoints.utilities.serializers.UserSerializer;
import org.FuelPoints.vessels.JsonUser;
import org.FuelPoints.entities.User;
import org.FuelPoints.services.TripRepository;
import org.FuelPoints.services.UserRepository;
import org.FuelPoints.services.VehicleRepository;
import org.FuelPoints.utilities.PasswordStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;


@RestController
public class UserController {
    @Autowired
    UserRepository users;
    @Autowired
    VehicleRepository vehicles;
    @Autowired
    TripRepository trips;

    RootSerializer rootSerializer = new RootSerializer();
    UserSerializer userSerializer = new UserSerializer();

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public HashMap<String, Object> login(HttpServletResponse response, @RequestBody RootParser<JsonUser> jsonUser) throws Exception {

        User user = users.findFirstByName(jsonUser.getData().getEntity().getName());
        if (user == null) {
            response.sendError(401, "Account does not exist.");
        } else if (!user.verifyPassword(jsonUser.getData().getEntity().getPassword())) {
            response.sendError(401, "Invalid credentials");
        }
        return rootSerializer.serializeOne(
                "/users/" + user.getId(),   //todo: what if user is null ??
                user,
                userSerializer);
    }

    @RequestMapping(path = "/users", method = RequestMethod.POST)
    public HashMap<String, Object> register(HttpServletResponse response, @RequestBody RootParser<JsonUser> parser) throws IOException, PasswordStorage.CannotPerformOperationException {
        JsonUser jsonUser = parser.getData().getEntity();
        //TODO: check for null

        //TODO: try
        User user = users.findFirstByName(jsonUser.getName());
        if (user != null) {
            response.sendError(401, "Username is not available.");
        } else {
            user = new User(jsonUser.getName(), jsonUser.getPassword());
            users.save(user);
            response.setStatus(201);
        }
        return rootSerializer.serializeOne(
                "/users/" + user.getId(),
                user,                                 //todo: what if user is null?
                userSerializer);

    }

}


