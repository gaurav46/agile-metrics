package at.grisa.agilemetrics.producer.jirasoftwareserver;

import at.grisa.agilemetrics.producer.atlassian.rest.RestClient;
import at.grisa.agilemetrics.producer.atlassian.rest.entities.QueryParam;
import at.grisa.agilemetrics.producer.jirasoftwareserver.restentities.*;
import at.grisa.agilemetrics.producer.jirasoftwareserver.restentities.greenhopper.RapidView;
import at.grisa.agilemetrics.producer.jirasoftwareserver.restentities.greenhopper.RapidViews;
import at.grisa.agilemetrics.producer.jirasoftwareserver.restentities.greenhopper.SprintReport;
import at.grisa.agilemetrics.producer.jirasoftwareserver.restentities.greenhopper.Sprints;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class JiraSoftwareServerRestClient {
    private final RestClient restClient;

    public JiraSoftwareServerRestClient(String hostUrl, String user, String password) {
        this.restClient = new RestClient(hostUrl, user, password);
    }

    public Collection<Board> getScrumBoards() {
        String boardsPath = "/rest/agile/1.0/board";
        LinkedList<Board> boards = new LinkedList<>();
        Integer startElementIndex = 0;
        Boolean boardsToLoad = true;

        while (boardsToLoad) {
            QueryParam startElement = new QueryParam("start", startElementIndex);
            PagedEntities<Board> pagedBoards = restClient.getPagedEntities(new ParameterizedTypeReference<PagedEntities<Board>>() {
            }, boardsPath, startElement);

            for (Board board : pagedBoards.getValues()) {
                if (board.getType().toLowerCase().equals("scrum")) {
                    boards.add(board);
                }
            }

            startElementIndex = pagedBoards.getStartAt() + pagedBoards.getMaxResults();
            boardsToLoad = pagedBoards.getLast() != null && !pagedBoards.getLast();
        }

        return boards;
    }

    public String getScrumBoardJQLFilter(Long boardId) {
        String boardConfigPath = "/rest/agile/1.0/board/{boardId}/configuration";
        String boardConfigRequestPath = boardConfigPath.replace("{boardId}", boardId.toString());
        BoardConfiguration boardConfiguration = restClient.getEntity(BoardConfiguration.class, boardConfigRequestPath);

        String boardFilterPath = "/rest/api/2/filter/{filterId}";
        String boardFilterRequestPath = boardFilterPath.replace("{filterId}", boardConfiguration.getFilter().getId().toString());
        Filter boardFilter = restClient.getEntity(Filter.class, boardFilterRequestPath);

        return boardFilter.getJql();
    }

    public Issue getIssue(Long issueId, QueryParam... queryParams) {
        String issuePath = "/rest/api/2/issue/{issueId}";
        String issueRequestPath = issuePath.replace("{issueId}", issueId.toString());
        Issue issue = restClient.getEntity(Issue.class, issueRequestPath, queryParams);

        return issue;
    }

    public Collection<Issue> getIssuesByJQL(String jql) {
        String searchPath = "/rest/api/2/search";
        QueryParam jqlQueryParam = new QueryParam("jql", jql);

        LinkedList<Issue> issues = new LinkedList<>();
        Integer startElementIndex = 0;
        Boolean issuesToLoad = true;

        while (issuesToLoad) {
            QueryParam startElementIndexQueryParam = new QueryParam("start", startElementIndex);
            PagedEntities<Issue> pagedIssues = restClient.getPagedEntities(new ParameterizedTypeReference<PagedEntities<Issue>>() {
            }, searchPath, jqlQueryParam, startElementIndexQueryParam);

            issues.addAll(Arrays.asList(pagedIssues.getIssues()));

            startElementIndex = pagedIssues.getStartAt() + pagedIssues.getMaxResults();
            issuesToLoad = pagedIssues.getLast() != null && !pagedIssues.getLast();
        }

        return issues;
    }

    public Sprint getActiveSprint(Long board) {
        String sprintsPath = "/rest/agile/1.0/board/{boardId}/sprint";
        String requestPath = sprintsPath.replace("{boardId}", board.toString());
        Integer startElementIndex = 0;
        Boolean sprintsToLoad = true;

        while (sprintsToLoad) {
            QueryParam startElement = new QueryParam("start", startElementIndex);
            PagedEntities<Sprint> pagedSprints = restClient.getPagedEntities(new ParameterizedTypeReference<PagedEntities<Sprint>>() {
            }, requestPath, startElement);

            for (Sprint sprint : pagedSprints.getValues()) {
                if (sprint.getState().equals("active")) {
                    return sprint;
                }
            }

            startElementIndex = pagedSprints.getStartAt() + pagedSprints.getMaxResults();
            sprintsToLoad = pagedSprints.getLast() != null && !pagedSprints.getLast();
        }

        return null;
    }

    public Integer getSprintIssuesCount(Long boardId, Long sprintId) {
        String issuesPath = "/rest/agile/1.0/board/{boardId}/sprint/{sprintId}/issue";
        String requestPath = issuesPath.replace("{boardId}", boardId.toString()).replace("{sprintId}", sprintId.toString());
        LinkedList<Issue> issues = new LinkedList<>();
        Integer startElementIndex = 0;
        Boolean issuesToLoad = true;

        PagedEntities<Issue> pagedIssues = restClient.getPagedEntities(new ParameterizedTypeReference<PagedEntities<Issue>>() {
        }, requestPath);

        return pagedIssues.getTotal();
    }

    public Collection<Issue> getSprintIssues(Long boardId, Long sprintId, QueryParam additionalParameter) {
        String issuesPath = "/rest/agile/1.0/board/{boardId}/sprint/{sprintId}/issue";
        String requestPath = issuesPath.replace("{boardId}", boardId.toString()).replace("{sprintId}", sprintId.toString());
        LinkedList<Issue> issues = new LinkedList<>();
        Integer startElementIndex = 0;
        Boolean issuesToLoad = true;

        while (issuesToLoad) {
            QueryParam startElement = new QueryParam("start", startElementIndex);

            PagedEntities<Issue> pagedIssues = null;
            if (additionalParameter != null) {
                pagedIssues = restClient.getPagedEntities(new ParameterizedTypeReference<PagedEntities<Issue>>() {
                }, requestPath, startElement, additionalParameter);
            } else {
                pagedIssues = restClient.getPagedEntities(new ParameterizedTypeReference<PagedEntities<Issue>>() {
                }, requestPath, startElement);
            }

            issues.addAll(Arrays.asList(pagedIssues.getIssues()));

            startElementIndex = pagedIssues.getStartAt() + pagedIssues.getMaxResults();
            issuesToLoad = pagedIssues.getLast() != null && !pagedIssues.getLast();
        }

        return issues;
    }

    public Collection<Issue> getSprintIssues(Long boardId, Long sprintId) {
        return this.getSprintIssues(boardId, sprintId, null);
    }

    public Collection<Issue> getSprintIssuesStatus(Long boardId, Long sprintId) {
        return this.getSprintIssues(boardId, sprintId, new QueryParam("fields", "status"));
    }

    public Collection<RapidView> getRapidViewsGreenhopper() {
        String rapidviewsPath = "/rest/greenhopper/1.0/rapidview";

        RapidViews rapidViewsResponse = restClient.getEntity(RapidViews.class, rapidviewsPath);
        List<RapidView> rapidViews = Arrays.stream(rapidViewsResponse.getViews())
                .filter(rapidView -> rapidView.getSprintSupportEnabled())
                .collect(Collectors.toList());

        return rapidViews;
    }

    public at.grisa.agilemetrics.producer.jirasoftwareserver.restentities.greenhopper.Sprint getActiveSprintGreenhopper(Long rapidviewId) {
        String sprintsPath = "/rest/greenhopper/1.0/sprintquery/{rapidViewId}";
        String sprintsRequestPath = sprintsPath.replace("{rapidViewId}", rapidviewId.toString());

        Sprints sprintsResponse = restClient.getEntity(Sprints.class, sprintsRequestPath);
        List<at.grisa.agilemetrics.producer.jirasoftwareserver.restentities.greenhopper.Sprint> sprints = Arrays.stream(sprintsResponse.getSprints())
                .filter(sprint -> sprint.getState().toLowerCase().equals("active"))
                .collect(Collectors.toList());

        return sprints.size() > 0 ? sprints.get(0) : null;
    }

    public SprintReport getSprintReportGreenhopper(Long rapidviewId, Long sprintId) {
        String sprintReportPath = "/rest/greenhopper/1.0/rapid/charts/sprintreport";

        QueryParam rapidViewIdQueryParam = new QueryParam("rapidViewId", rapidviewId);
        QueryParam sprintIdQueryParam = new QueryParam("sprintId", sprintId);

        SprintReport sprintReport = restClient.getEntity(SprintReport.class, sprintReportPath, rapidViewIdQueryParam, sprintIdQueryParam);

        return sprintReport;
    }

    public VelocityReport getVelocityReportGreenhopper(Long rapidviewId) {
        String velocityReportPath = "/rest/greenhopper/1.0/rapid/charts/velocity";

        QueryParam rapidViewIdQueryParam = new QueryParam("rapidViewId", rapidviewId);

        VelocityReport velocityReport = restClient.getEntity(VelocityReport.class, velocityReportPath, rapidViewIdQueryParam);

        return velocityReport;
    }
}
