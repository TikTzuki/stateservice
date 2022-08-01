package org.minerva.stateservice.hrm.user;

import org.jetbrains.annotations.NotNull;
import org.minerva.stateservice.hrm.models.Allocate;
import org.minerva.stateservice.hrm.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequestMapping
public class UserControllerImpl implements UserController {
    @Autowired
    UserService userService;

    @NotNull
    @PostMapping(
            "users/allocate"
    )
    public ResponseEntity<?> allocate(@NotNull Allocate allocate) {
        return ResponseEntity.noContent().build();
    }

    @Override
    public void createUser(@NotNull User user) {

    }

}
