/**
 * Copyright (c) 2010 KUBO Atsuhiro <kubo@iteman.jp>,
 * All rights reserved.
 *
 * This file is part of MakeGood.
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package com.piece_framework.makegood.stagehand_testrunner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class StagehandTestRunner {
    private static final String BUNDLE_BASE_DIR = "/resources/php"; //$NON-NLS-1$
    private static final String[] BUNDLE_INCLUDE_PATH = {
        BUNDLE_BASE_DIR + "/PEAR/src", //$NON-NLS-1$
        BUNDLE_BASE_DIR + "/PEAR" //$NON-NLS-1$
    };
    private static final String BUNDLE_BIN_DIR = BUNDLE_BASE_DIR + "/bin"; //$NON-NLS-1$
    private static final HashMap<String, String> RUNNER_SCRIPTS =
        new HashMap<String, String>();

    static {
        RUNNER_SCRIPTS.put("phpunit", BUNDLE_BIN_DIR + "/phpunitrunner"); //$NON-NLS-1$ //$NON-NLS-2$
        RUNNER_SCRIPTS.put("simpletest", BUNDLE_BIN_DIR + "/simpletestrunner"); //$NON-NLS-1$ //$NON-NLS-2$
        RUNNER_SCRIPTS.put("phpt", BUNDLE_BIN_DIR + "/phptrunner"); //$NON-NLS-1$ //$NON-NLS-2$
        RUNNER_SCRIPTS.put("phpspec", BUNDLE_BIN_DIR + "/phpspecrunner"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String[] getBundleIncludePath() {
        List<String> includePaths = new ArrayList<String>();
        for (String path: BUNDLE_INCLUDE_PATH) {
            URL url;

            try {
                url = FileLocator.resolve(
                          Platform.getBundle(Activator.PLUGIN_ID).getEntry(path)
                      );
            } catch (IOException e) {
                Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
                break;
            }

            includePaths.add(new File(url.getPath()).getAbsolutePath());
        }

        return includePaths.toArray(new String[ includePaths.size() ]);
    }

    public static String getCommandPath(String framework)
        throws CoreException {
        URL url;

        try {
            url = FileLocator.resolve(
                    Platform.getBundle(Activator.PLUGIN_ID)
                            .getEntry(RUNNER_SCRIPTS.get(framework.toLowerCase()))
            );
        } catch (IOException e) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
        }

        return new File(url.getPath()).getAbsolutePath();
    }
}