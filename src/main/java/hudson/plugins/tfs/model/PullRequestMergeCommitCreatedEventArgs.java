package hudson.plugins.tfs.model;

import net.sf.ezmorph.MorpherRegistry;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.JSONUtils;
import net.sf.json.util.JavaIdentifierTransformer;

import java.util.HashMap;

public class PullRequestMergeCommitCreatedEventArgs extends GitCodePushedEventArgs {

    static final String SAMPLE_REQUEST_PAYLOAD =
        "{\n" +
        "    \"collectionUri\":\"https://fabrikam-fiber-inc.visualstudio.com\",\n" +
        "    \"repoUri\":\"https://fabrikam-fiber-inc.visualstudio.com/Personal/_git/olivida.tfs-plugin\",\n" +
        "    \"projectId\":\"Personal\",\n" +
        "    \"repoId\":\"olivida.tfs-plugin\",\n" +
        "    \"commit\":\"6a23fc7afec31f0a14bade6544bed4f16492e6d2\",\n" +
        "    \"pushedBy\":\"olivida\",\n" +
        "    \"pullRequestId\":42,\n" +
        "    \"iterationId\":2,\n" +
        "    \"workItems\":\n" +
        "    [\n" +
        "        {\n" +
        "          \"id\": 297,\n" +
        "          \"rev\": 1,\n" +
        "          \"fields\": {\n" +
        "            \"System.Id\": 297,\n" +
        "            \"System.AreaId\": 3570,\n" +
        "            \"System.AreaPath\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.NodeName\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.TeamProject\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.AreaLevel1\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.Rev\": 1,\n" +
        "            \"System.AuthorizedDate\": \"2014-12-29T20:49:20.77Z\",\n" +
        "            \"System.RevisedDate\": \"9999-01-01T00:00:00Z\",\n" +
        "            \"System.IterationId\": 3570,\n" +
        "            \"System.IterationPath\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.IterationLevel1\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.WorkItemType\": \"Product Backlog Item\",\n" +
        "            \"System.State\": \"New\",\n" +
        "            \"System.Reason\": \"New backlog item\",\n" +
        "            \"System.CreatedDate\": \"2014-12-29T20:49:20.77Z\",\n" +
        "            \"System.CreatedBy\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.ChangedDate\": \"2014-12-29T20:49:20.77Z\",\n" +
        "            \"System.ChangedBy\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.AuthorizedAs\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.PersonId\": 77331,\n" +
        "            \"System.Watermark\": 607,\n" +
        "            \"System.Title\": \"Customer can sign in using their Microsoft Account\",\n" +
        "            \"Microsoft.VSTS.Scheduling.Effort\": 8,\n" +
        "            \"WEF_6CB513B6E70E43499D9FC94E5BBFB784_System.ExtensionMarker\": true,\n" +
        "            \"WEF_6CB513B6E70E43499D9FC94E5BBFB784_Kanban.Column\": \"New\",\n" +
        "            \"System.Description\": \"Our authorization logic needs to allow for users with Microsoft Accounts (formerly Windows Live IDs) - http://msdn.microsoft.com/en-us/library/live/hh826547.aspx\"\n" +
        "          },\n" +
        "          \"relations\": [\n" +
        "            {\n" +
        "              \"rel\": \"System.LinkTypes.Hierarchy-Forward\",\n" +
        "              \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/299\",\n" +
        "              \"attributes\": {\n" +
        "                \"isLocked\": false,\n" +
        "                \"comment\": \"decomposition of work\"\n" +
        "              }\n" +
        "            },\n" +
        "            {\n" +
        "              \"rel\": \"System.LinkTypes.Hierarchy-Forward\",\n" +
        "              \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/300\",\n" +
        "              \"attributes\": {\n" +
        "                \"isLocked\": false\n" +
        "              }\n" +
        "            },\n" +
        "            {\n" +
        "              \"rel\": \"AttachedFile\",\n" +
        "              \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/attachments/098a279a-60b9-40a8-868b-b7fd00c0a439\",\n" +
        "              \"attributes\": {\n" +
        "                \"authorizedDate\": \"2014-12-29T20:49:20.77Z\",\n" +
        "                \"id\": 65273,\n" +
        "                \"resourceCreatedDate\": \"2014-12-29T20:49:20.77Z\",\n" +
        "                \"resourceModifiedDate\": \"2014-12-29T20:49:20.77Z\",\n" +
        "                \"revisedDate\": \"9999-01-01T00:00:00Z\",\n" +
        "                \"comment\": \"Spec for the work\",\n" +
        "                \"name\": \"Spec.txt\"\n" +
        "              }\n" +
        "            }\n" +
        "          ],\n" +
        "          \"_links\": {\n" +
        "            \"self\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/297\"\n" +
        "            },\n" +
        "            \"workItemUpdates\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/297/updates\"\n" +
        "            },\n" +
        "            \"workItemRevisions\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/297/revisions\"\n" +
        "            },\n" +
        "            \"workItemHistory\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/297/history\"\n" +
        "            },\n" +
        "            \"html\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/web/wi.aspx?pcguid=d81542e4-cdfa-4333-b082-1ae2d6c3ad16&id=297\"\n" +
        "            },\n" +
        "            \"workItemType\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/6ce954b1-ce1f-45d1-b94d-e6bf2464ba2c/_apis/wit/workItemTypes/Product%20Backlog%20Item\"\n" +
        "            },\n" +
        "            \"fields\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/fields\"\n" +
        "            }\n" +
        "          },\n" +
        "          \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/297\"\n" +
        "        },\n" +
        "        {\n" +
        "          \"id\": 299,\n" +
        "          \"rev\": 7,\n" +
        "          \"fields\": {\n" +
        "            \"System.Id\": 299,\n" +
        "            \"System.AreaId\": 4486,\n" +
        "            \"System.AreaPath\": \"Fabrikam-Fiber-Git\\\\Website\",\n" +
        "            \"System.NodeName\": \"Website\",\n" +
        "            \"System.TeamProject\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.AreaLevel1\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.AreaLevel2\": \"Website\",\n" +
        "            \"System.Rev\": 7,\n" +
        "            \"System.AuthorizedDate\": \"2014-12-29T20:49:28.74Z\",\n" +
        "            \"System.RevisedDate\": \"9999-01-01T00:00:00Z\",\n" +
        "            \"System.IterationId\": 3570,\n" +
        "            \"System.IterationPath\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.IterationLevel1\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.WorkItemType\": \"Task\",\n" +
        "            \"System.State\": \"To Do\",\n" +
        "            \"System.Reason\": \"New task\",\n" +
        "            \"System.AssignedTo\": \"Johnnie McLeod <fabrikamfiber2@hotmail.com>\",\n" +
        "            \"System.CreatedDate\": \"2014-12-29T20:49:21.617Z\",\n" +
        "            \"System.CreatedBy\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.ChangedDate\": \"2014-12-29T20:49:28.74Z\",\n" +
        "            \"System.ChangedBy\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.AuthorizedAs\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.PersonId\": 77331,\n" +
        "            \"System.Watermark\": 616,\n" +
        "            \"System.Title\": \"JavaScript implementation for Microsoft Account\",\n" +
        "            \"Microsoft.VSTS.Scheduling.RemainingWork\": 4,\n" +
        "            \"System.Description\": \"Follow the code samples from MSDN\",\n" +
        "            \"System.Tags\": \"Tag1; Tag2\"\n" +
        "          },\n" +
        "          \"relations\": [\n" +
        "            {\n" +
        "              \"rel\": \"System.LinkTypes.Hierarchy-Reverse\",\n" +
        "              \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/297\",\n" +
        "              \"attributes\": {\n" +
        "                \"isLocked\": false,\n" +
        "                \"comment\": \"decomposition of work\"\n" +
        "              }\n" +
        "            },\n" +
        "            {\n" +
        "              \"rel\": \"System.LinkTypes.Related\",\n" +
        "              \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/300\",\n" +
        "              \"attributes\": {\n" +
        "                \"isLocked\": false,\n" +
        "                \"comment\": \"adding another task\"\n" +
        "              }\n" +
        "            },\n" +
        "            {\n" +
        "              \"rel\": \"Hyperlink\",\n" +
        "              \"url\": \"http://blogs.msdn.com/b/bharry/archive/2014/05/12/a-new-api-for-visual-studio-online.aspx\",\n" +
        "              \"attributes\": {\n" +
        "                \"authorizedDate\": \"2014-12-29T20:49:27.98Z\",\n" +
        "                \"id\": 65275,\n" +
        "                \"resourceCreatedDate\": \"2014-12-29T20:49:27.98Z\",\n" +
        "                \"resourceModifiedDate\": \"2014-12-29T20:49:27.98Z\",\n" +
        "                \"revisedDate\": \"9999-01-01T00:00:00Z\"\n" +
        "              }\n" +
        "            }\n" +
        "          ],\n" +
        "          \"_links\": {\n" +
        "            \"self\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/299\"\n" +
        "            },\n" +
        "            \"workItemUpdates\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/299/updates\"\n" +
        "            },\n" +
        "            \"workItemRevisions\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/299/revisions\"\n" +
        "            },\n" +
        "            \"workItemHistory\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/299/history\"\n" +
        "            },\n" +
        "            \"html\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/web/wi.aspx?pcguid=d81542e4-cdfa-4333-b082-1ae2d6c3ad16&id=299\"\n" +
        "            },\n" +
        "            \"workItemType\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/6ce954b1-ce1f-45d1-b94d-e6bf2464ba2c/_apis/wit/workItemTypes/Task\"\n" +
        "            },\n" +
        "            \"fields\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/fields\"\n" +
        "            }\n" +
        "          },\n" +
        "          \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/299\"\n" +
        "        },\n" +
        "        {\n" +
        "          \"id\": 300,\n" +
        "          \"rev\": 1,\n" +
        "          \"fields\": {\n" +
        "            \"System.Id\": 300,\n" +
        "            \"System.AreaId\": 3570,\n" +
        "            \"System.AreaPath\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.NodeName\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.TeamProject\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.AreaLevel1\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.Rev\": 1,\n" +
        "            \"System.AuthorizedDate\": \"2014-12-29T20:49:22.103Z\",\n" +
        "            \"System.RevisedDate\": \"9999-01-01T00:00:00Z\",\n" +
        "            \"System.IterationId\": 3570,\n" +
        "            \"System.IterationPath\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.IterationLevel1\": \"Fabrikam-Fiber-Git\",\n" +
        "            \"System.WorkItemType\": \"Task\",\n" +
        "            \"System.State\": \"To Do\",\n" +
        "            \"System.Reason\": \"New task\",\n" +
        "            \"System.CreatedDate\": \"2014-12-29T20:49:22.103Z\",\n" +
        "            \"System.CreatedBy\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.ChangedDate\": \"2014-12-29T20:49:22.103Z\",\n" +
        "            \"System.ChangedBy\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.AuthorizedAs\": \"Jamal Hartnett <fabrikamfiber4@hotmail.com>\",\n" +
        "            \"System.PersonId\": 77331,\n" +
        "            \"System.Watermark\": 610,\n" +
        "            \"System.Title\": \"Unit Testing for MSA login\",\n" +
        "            \"Microsoft.VSTS.Scheduling.RemainingWork\": 3,\n" +
        "            \"System.Description\": \"We need to ensure we have coverage to prevent regressions\"\n" +
        "          },\n" +
        "          \"relations\": [\n" +
        "            {\n" +
        "              \"rel\": \"System.LinkTypes.Hierarchy-Reverse\",\n" +
        "              \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/297\",\n" +
        "              \"attributes\": {\n" +
        "                \"isLocked\": false\n" +
        "              }\n" +
        "            },\n" +
        "            {\n" +
        "              \"rel\": \"System.LinkTypes.Related\",\n" +
        "              \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/299\",\n" +
        "              \"attributes\": {\n" +
        "                \"isLocked\": false,\n" +
        "                \"comment\": \"adding another task\"\n" +
        "              }\n" +
        "            }\n" +
        "          ],\n" +
        "          \"_links\": {\n" +
        "            \"self\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/300\"\n" +
        "            },\n" +
        "            \"workItemUpdates\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/300/updates\"\n" +
        "            },\n" +
        "            \"workItemRevisions\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/300/revisions\"\n" +
        "            },\n" +
        "            \"workItemHistory\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/300/history\"\n" +
        "            },\n" +
        "            \"html\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/web/wi.aspx?pcguid=d81542e4-cdfa-4333-b082-1ae2d6c3ad16&id=300\"\n" +
        "            },\n" +
        "            \"workItemType\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/6ce954b1-ce1f-45d1-b94d-e6bf2464ba2c/_apis/wit/workItemTypes/Task\"\n" +
        "            },\n" +
        "            \"fields\": {\n" +
        "              \"href\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/fields\"\n" +
        "            }\n" +
        "          },\n" +
        "          \"url\": \"https://fabrikam-fiber-inc.visualstudio.com/_apis/wit/workItems/300\"\n" +
        "        }\n" +
        "    ]\n" +
        "}";
    public int pullRequestId;
    public int iterationId = -1;

    public static PullRequestMergeCommitCreatedEventArgs fromJsonString(final String jsonString) {
        final JSONObject jsonObject = JSONObject.fromObject(jsonString);
        return fromJsonObject(jsonObject);
    }

    static PullRequestMergeCommitCreatedEventArgs fromJsonObject(final JSONObject jsonObject) {
        final PullRequestMergeCommitCreatedEventArgs result;

        final JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.setRootClass(PullRequestMergeCommitCreatedEventArgs.class);
        final HashMap<String, Class> classMap = new HashMap<String, Class>();
        // TODO: the classMap is used without context, every property named here will try to use the type
        classMap.put("workItems", WorkItem.class);
        classMap.put("html", Link.class);
        jsonConfig.setClassMap(classMap);
        jsonConfig.setJavaIdentifierTransformer(new JavaIdentifierTransformer() {
            @Override
            public String transformToJavaIdentifier(final String str) {
                return str.replace(".", "_");
            }
        });

        final MorpherRegistry registry = JSONUtils.getMorpherRegistry();

        // TODO: I don't like messing with singletons; is there a way to do this with JsonConfig?
        registry.registerMorpher(URIMorpher.INSTANCE);
        try {
            result = (PullRequestMergeCommitCreatedEventArgs) JSONObject.toBean(jsonObject, jsonConfig);
        }
        finally {
            registry.deregisterMorpher(URIMorpher.INSTANCE);
        }

        return result;
    }

}
