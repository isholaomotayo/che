/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.factory.server;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.factory.Factory;
import org.eclipse.che.api.factory.server.model.impl.AuthorImpl;
import org.eclipse.che.api.factory.server.model.impl.FactoryImpl;
import org.eclipse.che.api.factory.server.spi.FactoryDao;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.lang.Pair;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.requireNonNull;

/**
 * @author Anton Korneta
 */
@Singleton
public class FactoryManager {

    private final FactoryDao factoryDao;

    @Inject
    public FactoryManager(FactoryDao factoryDao) {
        this.factoryDao = factoryDao;
    }

    /**
     * Stores {@link Factory} instance and related set of {@link FactoryImage}.
     *
     * @param factory
     *         instance of factory which would be stored
     * @return factory which has been stored
     * @throws NullPointerException
     *         when {@code factory} is null
     * @throws ConflictException
     *         when any conflict occurs (e.g Factory with given name already exists for {@code creator})
     * @throws ServerException
     *         when any server errors occurs
     */
    public Factory saveFactory(Factory factory) throws ConflictException,
                                                                                 ServerException {
        requireNonNull(factory);
        final FactoryImpl newFactory = new FactoryImpl(factory);
        newFactory.setId(NameGenerator.generate("factory", 16));
        if (isNullOrEmpty(newFactory.getName())) {
           newFactory.setName(NameGenerator.generate("f", 9));
        }
        return factoryDao.create(newFactory);
    }

    /**
     * Updates factory in accordance to the new configuration.
     *
     * <p>Note: Updating uses replacement strategy,
     * therefore existing factory would be replaced with given update {@code update}
     *
     * @param update
     *         factory update
     * @return updated factory
     * @throws NullPointerException
     *         when {@code update} is null
     * @throws ConflictException
     *         when any conflict occurs (e.g Factory with given name already exists for {@code creator})
     * @throws NotFoundException
     *         when factory with given id not found
     * @throws ServerException
     *         when any server error occurs
     */
    public Factory updateFactory(Factory update) throws ConflictException,
                                                                                  NotFoundException,
                                                                                  ServerException {
        requireNonNull(update);
        final AuthorImpl creator = factoryDao.getById(update.getId()).getCreator();
        return factoryDao.update(FactoryImpl.builder()
                                            .from(new FactoryImpl(update))
                                            .setCreator(new AuthorImpl(creator.getUserId(), creator.getCreated()))
                                            .build());
    }

    /**
     * Removes stored {@link Factory} by given id.
     *
     * @param id
     *         factory identifier
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws ServerException
     *         when any server errors occurs
     */
    public void removeFactory(String id) throws ServerException {
        requireNonNull(id);
        factoryDao.remove(id);
    }

    /**
     * Gets factory by given id.
     *
     * @param id
     *         factory identifier
     * @return factory instance
     * @throws NullPointerException
     *         when {@code id} is null
     * @throws NotFoundException
     *         when factory with given id not found
     * @throws ServerException
     *         when any server errors occurs
     */
    public Factory getById(String id) throws NotFoundException,
                                             ServerException {
        requireNonNull(id);
        return factoryDao.getById(id);
    }

    /**
     * Get list of factories which conform specified attributes.
     *
     * @param maxItems
     *         max number of items in response
     * @param skipCount
     *         skip items. Must be equals or greater then {@code 0}
     * @param attributes
     *         skip items. Must be equals or greater then {@code 0}
     * @return stored data, if specified attributes is correct
     * @throws ServerException
     *         when any server errors occurs
     */
    @SuppressWarnings("unchecked")
    public <T extends List<? extends Factory>> T getByAttribute(int maxItems,
                                                                int skipCount,
                                                                List<Pair<String, String>> attributes) throws ServerException {
        return (T)factoryDao.getByAttribute(maxItems, skipCount, attributes);
    }
}
