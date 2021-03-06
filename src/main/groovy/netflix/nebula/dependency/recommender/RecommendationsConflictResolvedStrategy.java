package netflix.nebula.dependency.recommender;

import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencyResolveDetails;
import org.gradle.api.artifacts.ModuleVersionSelector;

import java.util.ArrayList;
import java.util.List;

/**
 * Recommendations are conflict resolved against transitive dependencies.  In the event that a transitive dependency
 * has a higher version of a library than a recommendation, the recommendation only wins if there is a first order
 * recommend-able dependency.
 */
public class RecommendationsConflictResolvedStrategy extends RecommendationStrategy {

    private List<String> firstOrderDepsWithoutVersions = new ArrayList<>();

    @Override
    public void inspectDependency(Dependency dependency) {
        if (isVersionMissing(dependency.getVersion())) {
            firstOrderDepsWithoutVersions.add(dependency.getGroup() + ":" + dependency.getName());
        }
    }

    @Override
    public boolean canRecommendVersion(ModuleVersionSelector selector) {
        return firstOrderDepsWithoutVersions.contains(getCoord(selector));
    }

    @Override
    public boolean recommendVersion(DependencyResolveDetails details, String version) {
        boolean recommenderHasValidVersion = (version != null);
        if (recommenderHasValidVersion && isFirstOrderMissingVersion(details)) {
            details.useVersion(version);
            return true;
        }
        return false;
    }

    private boolean isVersionMissing(String version) {
        return (version == null || version.isEmpty());
    }

    private boolean isFirstOrderMissingVersion(DependencyResolveDetails details) {
        boolean dependencyInFirstOrderList = firstOrderDepsWithoutVersions.contains(getCoord(details));
        boolean versionMissing = isVersionMissing(details.getRequested().getVersion());
        return dependencyInFirstOrderList && versionMissing;
    }
}
