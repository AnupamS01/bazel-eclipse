package com.salesforce.bazel.eclipse.core.resources;

import static com.salesforce.bazel.eclipse.core.BazelCoreSharedContstants.BAZEL_NATURE_ID;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IDynamicReferenceProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.salesforce.bazel.eclipse.core.BazelCore;

/**
 * Implementation of {@link IDynamicReferenceProvider} which ensures a Bazel project for a package/target is connected
 * to its related workspace package.
 */
public class BazelProjectReferencesProvider implements IDynamicReferenceProvider {

    @Override
    public List<IProject> getDependentProjects(IBuildConfiguration buildConfiguration) throws CoreException {
        var project = buildConfiguration.getProject();
        if (project.hasNature(BAZEL_NATURE_ID)) {
            var bazelProject = BazelCore.create(project);
            if (!bazelProject.isWorkspaceProject()) {
                // each non-workspace project automatically depends on its workspace project
                // (this is to ensure it's always available)

                var bazelWorkspace = bazelProject.getBazelWorkspace();
                if (bazelWorkspace.hasInfo()) {
                    // workspace is loaded use the model to lookup project
                    return List.of(bazelWorkspace.getBazelProject().getProject());
                }

                // note, due to https://github.com/eclipse-platform/eclipse.platform.ui/issues/1062 we create the project ourselves when not loaded
                var containers = project.getWorkspace()
                        .getRoot()
                        .findContainersForLocationURI(bazelWorkspace.getLocation().toPath().toUri());
                for (IContainer container : containers) {
                    if (container instanceof IProject workspaceProject) {
                        return List.of(workspaceProject);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

}
