/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.runner.client.tabs.properties.panel.common.docker;

import org.eclipse.che.ide.api.editor.EditorInput;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.project.tree.VirtualFile;
import com.google.gwt.resources.client.ImageResource;

import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.Nonnull;

/**
 * This class is copy of com.codenvy.ide.core.editor.EditorInputImpl.
 *
 * @author Vitaly Parfonov
 */
public class DockerFileEditorInput implements EditorInput {

    private final FileType    fileType;
    private       VirtualFile file;

    public DockerFileEditorInput(@Nonnull FileType fileType, @Nonnull VirtualFile file) {
        this.fileType = fileType;
        this.file = file;
    }

    /** {@inheritDoc} */
    @Override
    public String getContentDescription() {
        return fileType.getContentDescription();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getToolTipText() {
        return "";
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public String getName() {
        return file.getDisplayName();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public ImageResource getImageResource() {
        return fileType.getImage();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public SVGResource getSVGResource() {
        return fileType.getSVGImage();
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public VirtualFile getFile() {
        return file;
    }

    /** {@inheritDoc} */
    @Override
    public void setFile(@Nonnull VirtualFile file) {
        this.file = file;
    }

}