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
package org.eclipse.che.api.project.server.handlers;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.ProjectRegistry;

/**
 * Called after project initialized.
 *
 * @author gazarenkov
 */
public interface ProjectInitHandler extends ProjectHandler {

    /**
     * Handler to be fired after initialization of project.
     *
     * @param registry
     *         project registry
     * @param projectFolder
     *         base project folder
     * @throws ServerException
     * @throws ForbiddenException
     * @throws ConflictException
     * @throws NotFoundException
     */
    void onProjectInitialized(ProjectRegistry registry, FolderEntry projectFolder) throws ServerException,
                                                                                          ForbiddenException,
                                                                                          ConflictException,
                                                                                          NotFoundException;
}
