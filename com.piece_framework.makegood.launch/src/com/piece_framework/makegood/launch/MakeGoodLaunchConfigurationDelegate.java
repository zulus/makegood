package com.piece_framework.makegood.launch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class MakeGoodLaunchConfigurationDelegate implements ILaunchConfigurationDelegate {
    public void launch(ILaunchConfiguration configuration,
                       String mode,
                       ILaunch launch,
                       IProgressMonitor monitor
                       ) throws CoreException {
        ILaunchConfiguration stagehandTestRunnerLaunchConfiguration =
            createStagehandTestRunnerLaunchConfiguration(launch, configuration);

        ILaunch stagehandTestRunnerLaunch = replaceLaunch(launch, stagehandTestRunnerLaunchConfiguration);

        Set modes = new HashSet();
        modes.add(mode);
        ILaunchConfigurationType configurationType =
            DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurationType("org.eclipse.php.debug.core.launching.PHPExeLaunchConfigurationType");
        ILaunchDelegate delegate = configurationType.getDelegates(modes)[0];

        delegate.getDelegate().launch(stagehandTestRunnerLaunchConfiguration,
                                      mode,
                                      stagehandTestRunnerLaunch,
                                      monitor
                                      );

        if (MakeGoodViewRegistry.getViewId() == null) {
            return;
        }
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                try {
                    page.showView(MakeGoodViewRegistry.getViewId());
                } catch (PartInitException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    private ILaunchConfiguration createStagehandTestRunnerLaunchConfiguration(ILaunch launch,
                                                                              ILaunchConfiguration configuration
                                                                              ) throws CoreException {
        MakeGoodLauncher launcher = null;
        try {
            MakeGoodLauncherRegistry registry = new MakeGoodLauncherRegistry();
            launcher = registry.getLauncher(TestingFramework.PHPUnit);
        } catch (FileNotFoundException e) {
            throw new CoreException(new Status(IStatus.ERROR,
                                               Activator.PLUGIN_ID,
                                               0,
                                               e.getMessage(),
                                               e
                                               ));
        }

        String configurationName = "MakeGood" + Long.toString(System.currentTimeMillis());
        String log = MakeGoodLauncherRegistry.getRegistry().getAbsolutePath().toString() +
                     String.valueOf(File.separatorChar) +
                     configurationName +
                     ".xml";
        MakeGoodLaunchParameter parameter = MakeGoodLaunchParameter.get();

        ILaunchConfigurationWorkingCopy workingCopy = configuration.copy(configurationName);
        workingCopy.setAttribute("ATTR_FILE", parameter.getScript());
        workingCopy.setAttribute("ATTR_FILE_FULL_PATH", launcher.getScript());
        workingCopy.setAttribute("LOG_JUNIT", log);
        workingCopy.setAttribute("exeDebugArguments", parameter.generateParameter(log));
        return workingCopy;
    }

    private ILaunch replaceLaunch(ILaunch launch, ILaunchConfiguration configuration) {
        ILaunch newLaunch = new Launch(configuration,
                                       launch.getLaunchMode(),
                                       launch.getSourceLocator()
                                       );
        newLaunch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT,
                               launch.getAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT)
                               );
        newLaunch.setAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING,
                               launch.getAttribute(DebugPlugin.ATTR_CONSOLE_ENCODING)
                               );

        ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
        manager.removeLaunch(launch);
        manager.addLaunch(newLaunch);

        return newLaunch;
    }
}
