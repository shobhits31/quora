package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

import static com.upgrad.quora.service.common.GenericErrorCode.*;

@Service
public class AdminBusinessService {

    @Value("${user.admin.role}")
    private String adminRole;

    @Autowired
    private UserDao userDao;


    /** delete requested user from db if the requestor is authorized
     * @param uuid
     * @param authorizationToken
     * @throws AuthorizationFailedException
     * @throws UserNotFoundException
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteUser(final String uuid,final String authorizationToken) throws AuthorizationFailedException, UserNotFoundException {

        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);

        if (userAuthTokenEntity == null) {
            throw new AuthorizationFailedException(ATHR_001_ADMIN.getCode(), ATHR_001_ADMIN.getDefaultMessage());
        }

        if (userAuthTokenEntity.getLogoutAt() != null || userAuthTokenEntity.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new AuthorizationFailedException(ATHR_002_ADMIN.getCode(), ATHR_002_ADMIN.getDefaultMessage());
        }

        UserEntity userEntity = userDao.getUser(uuid);
        if (userEntity == null) {
            throw new UserNotFoundException(USR_001_ADMIN.getCode(), USR_001_ADMIN.getDefaultMessage());
        }
        if (!userAuthTokenEntity.getUser().getRole().equals(adminRole)) {
            throw new AuthorizationFailedException(ATHR_003_ADMIN.getCode(), ATHR_003_ADMIN.getDefaultMessage());
        }

        userDao.deleteUser(userEntity);

    }


}
