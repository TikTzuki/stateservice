package org.minerva.stateservice.hrm.user;

import org.jetbrains.annotations.NotNull;
import org.minerva.stateservice.hrm.models.Allocate;
import org.minerva.stateservice.hrm.models.User;
import org.minerva.stateservice.hrm.repos.AllocateRepos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    AllocateRepos allocateRepos;

    @Override
    public void allocate(@NotNull Allocate allocate) {
        allocateRepos.save(allocate);
    }

    @Override
    public void createUser(@NotNull User user) {

    }
}
