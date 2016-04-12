/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.jmap.cassandra.access;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.james.jmap.api.access.AccessToken;
import org.apache.james.jmap.api.access.AccessTokenRepository;
import org.apache.james.jmap.api.access.exceptions.InvalidAccessToken;

import com.datastax.driver.core.Session;
import com.google.common.base.Preconditions;
import com.jasongoodwin.monads.Try;

public class CassandraAccessTokenRepository implements AccessTokenRepository {

    private final CassandraAccessTokenDAO cassandraAccessTokenDAO;

    @Inject
    public CassandraAccessTokenRepository(Session session, @Named(TOKEN_EXPIRATION_IN_MS) long durationInMilliseconds) {
        this.cassandraAccessTokenDAO = new CassandraAccessTokenDAO(session, durationInMilliseconds);
    }

    @Override
    public CompletableFuture<Void> addToken(String username, AccessToken accessToken) {
        Preconditions.checkNotNull(username);
        Preconditions.checkArgument(! username.isEmpty(), "Username should not be empty");
        Preconditions.checkNotNull(accessToken);

        return cassandraAccessTokenDAO.addToken(username, accessToken);
    }

    @Override
    public CompletableFuture<Void> removeToken(AccessToken accessToken) {
        Preconditions.checkNotNull(accessToken);

        return cassandraAccessTokenDAO.removeToken(accessToken);
    }

    @Override
    public CompletableFuture<Try<String>> getUsernameFromToken(AccessToken accessToken) throws InvalidAccessToken {
        Preconditions.checkNotNull(accessToken);

        return cassandraAccessTokenDAO.getUsernameFromToken(accessToken)
            .thenApply(
                optional -> Try.ofFailable(
                    () -> optional.orElseThrow(
                        () -> new InvalidAccessToken(accessToken))));
    }
}