package de.jvstvshd.chillingfoxes.foxesbot.module.status.provider

import de.jvstvshd.chillingfoxes.foxesbot.config.Config
import de.jvstvshd.chillingfoxes.foxesbot.module.status.StatusData
import de.jvstvshd.chillingfoxes.foxesbot.module.status.StatusMetaData
import de.jvstvshd.chillingfoxes.foxesbot.module.status.StatusType
import org.kohsuke.github.GHIssue
import org.kohsuke.github.GHIssueState
import org.kohsuke.github.GitHub

class IssueStatusProvider(config: Config, private val repoUrl: String) : StatusProvider {

    private val github = GitHub.connectUsingOAuth(config.configData.baseData.gitHubToken)

    override fun provide(): StatusData {
        val repo = github.getRepository(repoUrl.substringAfter(".com/"))
        val map = mutableMapOf<String, StatusMetaData>()
        val subComponents = mutableSetOf<StatusMetaData>()
        for (issue in repo.getIssues(GHIssueState.OPEN)) {
            if (isSubComponent(issue)) {
                subComponents.add(StatusMetaData(issue.title, getStatusType(issue)))
            } else {
                map[issue.title] = StatusMetaData(issue.title, getStatusType(issue))
            }
        }
        for (subComponent in subComponents) {
            for (key in map.keys) {
                if (subComponent.name.startsWith(key, true)) {
                    map[key]?.children?.put(subComponent.name, subComponent)
                }
            }
        }
        return StatusData(repoUrl, map, repoUrl)
    }

    private fun getStatusType(issue: GHIssue): StatusType {
        for (label in issue.labels) {
            if (StatusType.canBeParsed(label.name)) {
                return StatusType.getByName(label.name)
            }
        }
        return StatusType.UNKNOWN
    }

    private fun isSubComponent(issue: GHIssue) =
        issue.labels.stream().anyMatch { it.name.lowercase() == "subcomponent" }
}