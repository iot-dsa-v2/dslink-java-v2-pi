package org.iot.dsa.pi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class WebApiMethod {

    private static PathTreeNode methods = new PathTreeNode();
    private String bodyParameterDescription = null;
    private String bodyParameterName = null;
    private String description;
    private String name;
    private String path;
    private boolean stream;
    private String type;
    private List<UrlParameter> urlParameters = new ArrayList<UrlParameter>();

    public WebApiMethod(String path, String type, String name, String description) {
        super();
        this.path = path;
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public static List<WebApiMethod> find(String path) {
        return methods.get(path);
    }

    public String getBodyParameterDescription() {
        return bodyParameterDescription;
    }

    public String getBodyParameterName() {
        return bodyParameterName;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public List<UrlParameter> getUrlParameters() {
        return urlParameters;
    }

    void addParameter(UrlParameter parameter) {
        urlParameters.add(parameter);
    }

    boolean isStream() {
        return stream;
    }

    void setStream(boolean isStream) {
        this.stream = isStream;
    }

    void setBodyParameter(String name, String description) {
        this.bodyParameterName = name;
        this.bodyParameterDescription = description;
    }

    private static class PathTreeNode {

        Map<String, PathTreeNode> children = new HashMap<String, PathTreeNode>();
        //String rootPath;
        List<WebApiMethod> leaves = new ArrayList<WebApiMethod>();

        List<WebApiMethod> get(String path) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            List<String> pathlist = new LinkedList<String>(Arrays.asList(path.split("/")));
            return find(pathlist);
        }

        void insert(WebApiMethod method) {
            String path = method.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            List<String> pathlist = new LinkedList<String>(Arrays.asList(path.split("/")));
            insert(method, pathlist);
        }

        private List<WebApiMethod> find(List<String> partialPath) {
            if (partialPath.isEmpty()) {
                return leaves;
            } else {
                String key = partialPath.remove(0);
                PathTreeNode child = children.get(key);
                if (child == null) {
                    for (String k : children.keySet()) {
                        if (k.startsWith("{") && k.endsWith("}")) {
                            return children.get(k).find(partialPath);
                        }
                    }
                    return new ArrayList<WebApiMethod>();
                }
                return child.find(partialPath);
            }
        }

        private void insert(WebApiMethod method, List<String> partialPath) {
            if (partialPath.isEmpty()) {
                leaves.add(method);
            } else {
                String key = partialPath.remove(0);
                PathTreeNode child = children.get(key);
                if (child == null) {
                    child = new PathTreeNode();
                    children.put(key, child);
                }
                child.insert(method, partialPath);
            }
        }
    }

    static class UrlParameter {

        private String description;
        private String name;
        private boolean required;
        private Class<?> type;

        UrlParameter(String name, Class<?> type, String description, boolean required) {
            this.name = name;
            this.type = type;
            this.description = description;
            this.required = required;
        }

        public String getDescription() {
            return description;
        }

        public String getName() {
            return name;
        }

        public Class<?> getType() {
            return type;
        }

        public boolean isRequired() {
            return required;
        }
    }

    static {
        WebApiMethod method;

        method = new WebApiMethod("/elementtemplates/{webId}/attributetemplates", "GET",
                                  "getAttributeTemplates",
                                  "Get child attribute templates for an element template.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("showInherited", Boolean.class,
                                             "Specifies if the result should include attribute templates inherited from base element templates. The default is 'false'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the element template.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the element template based on the specified criteria. By default, all security entries for this element template are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the element template for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the element template with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}", "GET", "get",
                                  "Retrieve an element template.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}", "PATCH", "update",
                                  "Update an element template by replacing items in its definition.");
        method.setBodyParameter("template",
                                "A partial element template containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates", "GET", "getByPath",
                                  "Retrieve an element template by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the element template..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the element template.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/categories", "GET", "getCategories",
                                  "Get an element template's categories.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("showInherited", Boolean.class,
                                             "Specifies if the result should include categories inherited from base element templates. The default is 'false'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/analysistemplates", "GET",
                                  "getAnalysisTemplates",
                                  "Get analysis templates for an element template.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the element template.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}/attributetemplates", "POST",
                                  "createAttributeTemplate", "Create an attribute template.");
        method.setBodyParameter("template", "The attribute template definition..");
        methods.insert(method);

        method = new WebApiMethod("/elementtemplates/{webId}", "DELETE", "delete",
                                  "Delete an element template.");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/eventframes", "POST", "createEventFrame",
                                  "Create an event frame.");
        method.setBodyParameter("eventFrame", "The new event frame definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the asset database for a specified user.");
        method.addParameter(new UrlParameter("securityItem", List.class,
                                             "The security item of the desired security information to be returned. Multiple security items may be specified with multiple instances of the parameter. If the parameter is not specified, only 'Default' security item of the security information will be returned..",
                                             true));
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/referencedelements", "GET",
                                  "getReferencedElements",
                                  "Retrieve referenced elements based on the specified conditions. By default, this method selects all referenced elements at the root level of the asset database.");
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned elements must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("descriptionFilter", String.class,
                                             "Specify that returned elements must have this description. The default is no description filter..",
                                             false));
        method.addParameter(new UrlParameter("elementType", String.class,
                                             "Specify that returned elements must have this type. The default type is 'Any'..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned elements must have this template or a template derived from this template. The default is no template filter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/tables", "GET", "getTables",
                                  "Retrieve tables for given Asset Database.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/analysistemplates", "POST",
                                  "createAnalysisTemplate",
                                  "Create an analysis template at the Asset Database root.");
        method.setBodyParameter("template", "The new analysis template definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/elementattributes", "GET",
                                  "findElementAttributes",
                                  "Retrieves a list of element attributes matching the specified filters from the specified asset database.");
        method.addParameter(new UrlParameter("attributeCategory", String.class,
                                             "Specify that returned attributes must have this category. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeDescriptionFilter", String.class,
                                             "The attribute description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeNameFilter", String.class,
                                             "The attribute name filter string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeType", String.class,
                                             "Specify that returned attributes' value type must be this value type. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementCategory", String.class,
                                             "Specify that the owner of the returned attributes must have this category. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementDescriptionFilter", String.class,
                                             "The element description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementNameFilter", String.class,
                                             "The element name filter string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementTemplate", String.class,
                                             "Specify that the owner of the returned attributes must have this template or a template derived from this template. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementType", String.class,
                                             "Specify that the element of the returned attributes must have this AFElementType. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned (the page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include objects nested further than immediate children of the given resource. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/export", "GET", "export",
                                  "Export the asset database.");
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "The latest ending time for AFEventFrame, AFTransfer, and AFCase objects that may be part of the export. The default is '*'..",
                                             false));
        method.addParameter(new UrlParameter("exportMode", List.class,
                                             "Indicates the type of export to perform. The default is 'StrongReferences'. Multiple export modes may be specified by using multiple instances of exportMode..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "The earliest starting time for AFEventFrame, AFTransfer, and AFCase objects that may be part of the export. The default is '*-30d'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/elementcategories", "GET",
                                  "getElementCategories",
                                  "Retrieve element categories for a given Asset Database.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/attributecategories", "POST",
                                  "createAttributeCategory",
                                  "Create an attribute category at the Asset Database root.");
        method.setBodyParameter("attributeCategory", "The new attribute category definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/elementtemplates", "POST",
                                  "createElementTemplate",
                                  "Create a template at the Asset Database root. Specify InstanceType of \"Element\" or \"EventFrame\" to create element or event frame template respectively. Only these two types of templates can be created.");
        method.setBodyParameter("template", "The new element template definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/tables", "POST", "createTable",
                                  "Create a table on the Asset Database.");
        method.setBodyParameter("table", "The new table definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/enumerationsets", "POST",
                                  "createEnumerationSet",
                                  "Create an enumeration set at the Asset Database.");
        method.setBodyParameter("enumerationSet", "The new enumeration set definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/eventframes", "GET", "getEventFrames",
                                  "Retrieve event frames based on the specified conditions. By default, returns all children of the specified root resource with a start time in the past 8 hours.");
        method.addParameter(new UrlParameter("canBeAcknowledged", Boolean.class,
                                             "Specify the returned event frames' canBeAcknowledged property. The default is no canBeAcknowledged filter..",
                                             false));
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned event frames must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "The ending time for the search. The endTime must be greater than or equal to the startTime. The searchMode parameter will control whether the comparison will be performed against the event frame's startTime or endTime. The default is '*' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("isAcknowledged", Boolean.class,
                                             "Specify the returned event frames' isAcknowledged property. The default no isAcknowledged filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding event frames. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("referencedElementNameFilter", String.class,
                                             "The name query string which must match the name of a referenced element. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("referencedElementTemplateName", String.class,
                                             "Specify that returned event frames must have an element in the event frame's referenced elements collection that derives from the template. Specify this parameter by name..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies whether the search should include objects nested further than the immediate children of the search root. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("searchMode", String.class,
                                             "Determines how the startTime and endTime parameters are treated when searching for event frame objects to be included in the returned collection. If this parameter is one of the 'Backward*' or 'Forward*' values, none of endTime, sortField, or sortOrder may be specified. The default is 'Overlapped'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("severity", List.class,
                                             "Specify that returned event frames must have this severity. Multiple severity values may be specified with multiple instances of the parameter. The default is no severity filter..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "The starting time for the search. startTime must be less than or equal to the endTime. The searchMode parameter will control whether the comparison will be performed against the event frame's startTime or endTime. The default is '*-8h'..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned event frames must have this template or a template derived from this template. The default is no template filter. Specify this parameter by name..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/elementtemplates", "GET",
                                  "getElementTemplates",
                                  "Retrieve element templates based on the specified criteria. Only templates of instance type \"Element\" and \"EventFrame\" are returned. By default, all element and event frame templates in the specified Asset Database are returned.");
        method.addParameter(new UrlParameter("field", List.class,
                                             "Specifies which of the object's properties are searched. Multiple search fields may be specified with multiple instances of the parameter. The default is 'Name'..",
                                             true));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("query", String.class,
                                             "The query string used for finding objects. The default is no query string..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/analysiscategories", "GET",
                                  "getAnalysisCategories",
                                  "Retrieve analysis categories for a given Asset Database.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries of the specified security item associated with the asset database based on the specified criteria. By default, all security entries for this asset database are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries information to be returned. If the parameter is not specified, security entries of the 'Default' security item will be returned..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}", "GET", "get",
                                  "Retrieve an Asset Database.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/elementcategories", "POST",
                                  "createElementCategory",
                                  "Create an element category at the Asset Database root.");
        method.setBodyParameter("elementCategory", "The new element category definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/analyses", "GET", "findAnalyses",
                                  "Retrieve analyses based on the specified conditions.");
        method.addParameter(new UrlParameter("field", List.class,
                                             "Specifies which of the object's properties are searched. Multiple search fields may be specified with multiple instances of the parameter. The default is 'Name'..",
                                             true));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("query", String.class,
                                             "The query string used for finding analyses. The default is null..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}", "PATCH", "update",
                                  "Update an asset database by replacing items in its definition.");
        method.setBodyParameter("database", "A partial database containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases", "GET", "getByPath",
                                  "Retrieve an Asset Database by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the database..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/referencedelements", "POST",
                                  "addReferencedElement",
                                  "Add a reference to an existing element to the specified database.");
        method.addParameter(new UrlParameter("referencedElementWebId", List.class,
                                             "The ID of the referenced element. Multiple referenced elements may be specified with multiple instances of the parameter..",
                                             true));
        method.addParameter(new UrlParameter("referenceType", String.class,
                                             "The name of the reference type between the parent and the referenced element. This must be a \"strong\" reference type. The default is \"parent-child\"..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/import", "POST", "importData",
                                  "Import an asset database.");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/attributecategories", "GET",
                                  "getAttributeCategories",
                                  "Retrieve attribute categories for a given Asset Database.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/elements", "POST", "createElement",
                                  "Create a child element.");
        method.setBodyParameter("element", "The new element definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the asset database.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries to be deleted. If the parameter is not specified, security entries of the 'Default' security item will be deleted..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/analysiscategories", "POST",
                                  "createAnalysisCategory",
                                  "Create an analysis category at the Asset Database root.");
        method.setBodyParameter("analysisCategory", "The new analysis category definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/eventframeattributes", "GET",
                                  "findEventFrameAttributes",
                                  "Retrieves a list of event frame attributes matching the specified filters from the specified asset database.");
        method.addParameter(new UrlParameter("attributeCategory", String.class,
                                             "Specify that returned attributes must have this category. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeDescriptionFilter", String.class,
                                             "The attribute description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeNameFilter", String.class,
                                             "The attribute name filter string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeType", String.class,
                                             "Specify that returned attributes' value type must be this value type. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "A string representing the latest ending time for the event frames to be matched. The endTime must be greater than or equal to the startTime. The default is '*'..",
                                             false));
        method.addParameter(new UrlParameter("eventFrameCategory", String.class,
                                             "Specify that the owner of the returned attributes must have this category. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("eventFrameDescriptionFilter", String.class,
                                             "The event frame description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("eventFrameNameFilter", String.class,
                                             "The event frame name filter string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("eventFrameTemplate", String.class,
                                             "Specify that the owner of the returned attributes must have this template or a template derived from this template. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned (the page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("referencedElementNameFilter", String.class,
                                             "The name query string which must match the name of a referenced element. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include objects nested further than immediate children of the given resource. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("searchMode", String.class,
                                             "Determines how the startTime and endTime parameters are treated when searching for event frames.     The default is 'Overlapped'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "A string representing the earliest starting time for the event frames to be matched. startTime must be less than or equal to the endTime. The default is '*-8h'..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/enumerationsets", "GET",
                                  "getEnumerationSets",
                                  "Retrieve enumeration sets for given asset database.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the asset database.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries to be updated. If the parameter is not specified, security entries of the 'Default' security item will be updated..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry of the specified security item associated with the asset database with the specified name.");
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries information to be returned. If the parameter is not specified, security entries of the 'Default' security item will be returned..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/referencedelements", "DELETE",
                                  "removeReferencedElement",
                                  "Remove a reference to an existing element from the specified database.");
        method.addParameter(new UrlParameter("referencedElementWebId", List.class,
                                             "The ID of the referenced element. Multiple referenced elements may be specified with multiple instances of the parameter..",
                                             true));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/tablecategories", "POST",
                                  "createTableCategory",
                                  "Create a table category on the Asset Database.");
        method.setBodyParameter("tableCategory", "The new table category definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/tablecategories", "GET",
                                  "getTableCategories",
                                  "Retrieve table categories for a given Asset Database.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/analysistemplates", "GET",
                                  "getAnalysisTemplates",
                                  "Retrieve analysis templates based on the specified criteria. By default, all analysis templates in the specified Asset Database are returned.");
        method.addParameter(new UrlParameter("field", List.class,
                                             "Specifies which of the object's properties are searched. Multiple search fields may be specified with multiple instances of the parameter. The default is 'Name'..",
                                             true));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("query", String.class,
                                             "The query string used for finding objects. The default is no query string..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/elements", "GET", "getElements",
                                  "Retrieve elements based on the specified conditions. By default, this method selects immediate children of the specified asset database.");
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned elements must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("descriptionFilter", String.class,
                                             "Specify that returned elements must have this description. The default is no description filter..",
                                             false));
        method.addParameter(new UrlParameter("elementType", String.class,
                                             "Specify that returned elements must have this type. The default type is 'Any'..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include objects nested further than the immediate children of the searchRoot. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned elements must have this template or a template derived from this template. The default is no template filter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the asset database.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries to be created. If the parameter is not specified, security entries of the 'Default' security item will be created..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetdatabases/{webId}", "DELETE", "delete",
                                  "Delete an asset database.");
        methods.insert(method);

        method = new WebApiMethod("/units/{webId}", "DELETE", "delete", "Delete a unit.");
        methods.insert(method);

        method = new WebApiMethod("/units/{webId}", "GET", "get", "Retrieve a unit.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/units/{webId}", "PATCH", "update", "Update a unit.");
        method.setBodyParameter("unitDTO", "A partial unit containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/units", "GET", "getByPath", "Retrieve a unit by path.");
        method.addParameter(new UrlParameter("path", String.class, "The path to the unit..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/recordedattimes", "GET", "getRecordedAtTimes",
                                  "Retrieves recorded values at the specified times.");
        method.addParameter(new UrlParameter("desiredUnits", String.class,
                                             "The name or abbreviation of the desired units of measure for the returned value, as found in the UOM database associated with the attribute. If not specified for an attribute, the attribute's default unit of measure is used. If the underlying stream is a point, this value may not be specified, as points are not associated with a unit of measure..",
                                             false));
        method.addParameter(new UrlParameter("retrievalMode", String.class,
                                             "An optional value that determines the value to return when a value doesn't exist at the exact time specified. The default is 'Auto'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("time", List.class,
                                             "The timestamp at which to retrieve a recorded value. Multiple timestamps may be specified with multiple instances of the parameter..",
                                             false));
        method.addParameter(new UrlParameter("timeZone", String.class,
                                             "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/plot", "GET", "getPlot",
                                  "Retrieves values over the specified time range suitable for plotting over the number of intervals (typically represents pixels).");
        method.addParameter(new UrlParameter("desiredUnits", String.class,
                                             "The name or abbreviation of the desired units of measure for the returned value, as found in the UOM database associated with the attribute. If not specified for an attribute, the attribute's default unit of measure is used. If the underlying stream is a point, this value may not be specified, as points are not associated with a unit of measure..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..",
                                             false));
        method.addParameter(new UrlParameter("intervals", Integer.class,
                                             "The number of intervals to plot over. Typically, this would be the number of horizontal pixels in the trend. The default is '24'. For each interval, the data available is examined and significant values are returned. Each interval can produce up to 5 values if they are unique, the first value in the interval, the last value, the highest value, the lowest value and at most one exceptional point (bad status or digital state)..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..",
                                             false));
        method.addParameter(new UrlParameter("timeZone", String.class,
                                             "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/value", "POST", "updateValue",
                                  "Updates a value for the specified stream.");
        method.setBodyParameter("value", "The value to add or update..");
        method.addParameter(new UrlParameter("bufferOption", String.class,
                                             "The desired AFBufferOption. The default is 'BufferIfPossible'..",
                                             false));
        method.addParameter(new UrlParameter("updateOption", String.class,
                                             "The desired AFUpdateOption. The default is 'Replace'. This parameter is ignored if the attribute is a configuration item..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/recorded", "GET", "getRecorded",
                                  "Returns a list of compressed values for the requested time range from the source provider.");
        method.addParameter(new UrlParameter("boundaryType", String.class,
                                             "An optional value that determines how the times and values of the returned end points are determined. The default is 'Inside'..",
                                             false));
        method.addParameter(new UrlParameter("desiredUnits", String.class,
                                             "The name or abbreviation of the desired units of measure for the returned value, as found in the UOM database associated with the attribute. If not specified for an attribute, the attribute's default unit of measure is used. If the underlying stream is a point, this value may not be specified, as points are not associated with a unit of measure..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..",
                                             false));
        method.addParameter(new UrlParameter("filterExpression", String.class,
                                             "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. The default is no filtering..",
                                             false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class,
                                             "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of values to be returned. The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..",
                                             false));
        method.addParameter(new UrlParameter("timeZone", String.class,
                                             "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/recordedattime", "GET", "getRecordedAtTime",
                                  "Returns a single recorded value based on the passed time and retrieval mode from the stream.");
        method.addParameter(new UrlParameter("time", String.class,
                                             "The timestamp at which the value is desired..",
                                             true));
        method.addParameter(new UrlParameter("desiredUnits", String.class,
                                             "The name or abbreviation of the desired units of measure for the returned value, as found in the UOM database associated with the attribute. If not specified for an attribute, the attribute's default unit of measure is used. If the underlying stream is a point, this value may not be specified, as points are not associated with a unit of measure..",
                                             false));
        method.addParameter(new UrlParameter("retrievalMode", String.class,
                                             "An optional value that determines the value to return when a value doesn't exist at the exact time specified. The default is 'Auto'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("timeZone", String.class,
                                             "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..",
                                             false));
        methods.insert(method);

        /*
        method = new WebApiMethod("/streams/{webId}/channel", "GET", "getChannel", "Opens a channel that will send messages about any value changes for the specified stream.");
        method.addParameter(new UrlParameter("includeInitialValues", Boolean.class, "Specified if the channel should send a message with the current value of the stream after the connection is opened. The default is 'false'..", false));
        methods.insert(method);
        */

        method = new WebApiMethod("/streams/{webId}/interpolated", "GET", "getInterpolated",
                                  "Retrieves interpolated values over the specified time range at the specified sampling interval.");
        method.addParameter(new UrlParameter("desiredUnits", String.class,
                                             "The name or abbreviation of the desired units of measure for the returned value, as found in the UOM database associated with the attribute. If not specified for an attribute, the attribute's default unit of measure is used. If the underlying stream is a point, this value may not be specified, as points are not associated with a unit of measure..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..",
                                             false));
        method.addParameter(new UrlParameter("filterExpression", String.class,
                                             "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. If the attribute does not support filtering, the filter will be ignored. The default is no filtering..",
                                             false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class,
                                             "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..",
                                             false));
        method.addParameter(new UrlParameter("interval", String.class,
                                             "The sampling interval, in AFTimeSpan format..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..",
                                             false));
        method.addParameter(new UrlParameter("timeZone", String.class,
                                             "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/recorded", "POST", "updateValues",
                                  "Updates multiple values for the specified stream.");
        method.setBodyParameter("values", "The values to add or update..");
        method.addParameter(new UrlParameter("bufferOption", String.class,
                                             "The desired AFBufferOption. The default is 'BufferIfPossible'..",
                                             false));
        method.addParameter(new UrlParameter("updateOption", String.class,
                                             "The desired AFUpdateOption. The default is 'Replace'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/summary", "GET", "getSummary",
                                  "Returns a summary over the specified time range for the stream.");
        method.addParameter(new UrlParameter("calculationBasis", String.class,
                                             "Specifies the method of evaluating the data over the time range. The default is 'TimeWeighted'..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..",
                                             false));
        method.addParameter(new UrlParameter("filterExpression", String.class,
                                             "A string containing a filter expression. Expression variables are relative to the attribute. Use '.' to reference the containing attribute..",
                                             false));
        method.addParameter(new UrlParameter("sampleInterval", String.class,
                                             "When the sampleType is Interval, sampleInterval specifies how often the filter expression is evaluated when computing the summary for an interval..",
                                             false));
        method.addParameter(new UrlParameter("sampleType", String.class,
                                             "Defines the evaluation of an expression over a time range. The default is 'ExpressionRecordedValues'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..",
                                             false));
        method.addParameter(new UrlParameter("summaryDuration", String.class,
                                             "The duration of each summary interval. If specified in hours, minutes, seconds, or milliseconds, the summary durations will be evenly spaced UTC time intervals. Longer interval types are interpreted using wall clock rules and are time zone dependent..",
                                             false));
        method.addParameter(new UrlParameter("summaryType", List.class,
                                             "Specifies the kinds of summaries to produce over the range. The default is 'Total'. Multiple summary types may be specified by using multiple instances of summaryType..",
                                             false));
        method.addParameter(new UrlParameter("timeType", String.class,
                                             "Specifies how to calculate the timestamp for each interval. The default is 'Auto'..",
                                             false));
        method.addParameter(new UrlParameter("timeZone", String.class,
                                             "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/value", "GET", "getValue",
                                  "Returns the value of the stream at the specified time. By default, this is usually the current value.");
        method.addParameter(new UrlParameter("desiredUnits", String.class,
                                             "The name or abbreviation of the desired units of measure for the returned value, as found in the UOM database associated with the attribute. If not specified for an attribute, the attribute's default unit of measure is used. If the underlying stream is a point, this value may not be specified, as points are not associated with a unit of measure..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("time", String.class,
                                             "An optional time. The default time context is determined from the owning object - for example, the time range of the event frame or transfer which holds this attribute. Otherwise, the implementation of the Data Reference determines the meaning of no context. For Points or simply configured PI Point Data References, this means the snapshot value of the PI Point on the Data Server..",
                                             false));
        method.addParameter(new UrlParameter("timeZone", String.class,
                                             "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/interpolatedattimes", "GET",
                                  "getInterpolatedAtTimes",
                                  "Retrieves interpolated values over the specified time range at the specified sampling interval.");
        method.addParameter(new UrlParameter("desiredUnits", String.class,
                                             "The name or abbreviation of the desired units of measure for the returned value, as found in the UOM database associated with the attribute. If not specified for an attribute, the attribute's default unit of measure is used. If the underlying stream is a point, this value may not be specified, as points are not associated with a unit of measure..",
                                             false));
        method.addParameter(new UrlParameter("filterExpression", String.class,
                                             "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. If the attribute does not support filtering, the filter will be ignored. The default is no filtering..",
                                             false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class,
                                             "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("time", List.class,
                                             "The timestamp at which to retrieve an interpolated value. Multiple timestamps may be specified with multiple instances of the parameter..",
                                             false));
        method.addParameter(new UrlParameter("timeZone", String.class,
                                             "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/streams/{webId}/end", "GET", "getEnd",
                                  "Returns the end-of-stream value of the stream.");
        method.addParameter(new UrlParameter("desiredUnits", String.class,
                                             "The name or abbreviation of the desired units of measure for the returned value, as found in the UOM database associated with the attribute. If not specified for an attribute, the attribute's default unit of measure is used. If the underlying stream is a point, this value may not be specified, as points are not associated with a unit of measure..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}/attributes", "POST", "createAttribute",
                                  "Create a new attribute as a child of the specified attribute.");
        method.setBodyParameter("attribute", "The definition of the new attribute..");
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}", "GET", "get", "Retrieve an attribute.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributes/multiple", "GET", "getMultiple",
                                  "Retrieve multiple attributes by web id or path.");
        method.addParameter(new UrlParameter("asParallel", Boolean.class,
                                             "Specifies if the retrieval processes should be run in parallel on the server. This may improve the response time for large amounts of requested attributes. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("includeMode", String.class,
                                             "The include mode for the return list. The default is 'All'..",
                                             false));
        method.addParameter(new UrlParameter("path", List.class,
                                             "The path of an attribute. Multiple attributes may be specified with multiple instances of the parameter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("webId", List.class,
                                             "The ID of an attribute. Multiple attributes may be specified with multiple instances of the parameter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}", "PATCH", "update",
                                  "Update an attribute by replacing items in its definition.");
        method.setBodyParameter("attribute",
                                "A partial attribute containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/attributes", "GET", "getByPath",
                                  "Retrieve an attribute by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the attribute..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}/attributes", "GET", "getAttributes",
                                  "Get the child attributes of the specified attribute.");
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned attributes must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding attributes. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class,
                                             "Specified if the search should include attributes with the Excluded property set. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class,
                                             "Specified if the search should include attributes with the Hidden property set. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned attributes must be members of this template. The default is no template filter..",
                                             false));
        method.addParameter(new UrlParameter("valueType", String.class,
                                             "Specify that returned attributes' value type must be the given value type. The default is no value type filter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}/value", "PUT", "setValue",
                                  "Set the value of a configuration item attribute. For attributes with a data reference or non-configuration item attributes, consult the documentation for streams.");
        method.setBodyParameter("value", "The value to write..");
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}/categories", "GET", "getCategories",
                                  "Get an attribute's categories.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}/config", "POST", "createConfig",
                                  "Create or update an attribute's DataReference configuration (Create/Update PI point for PI Point DataReference).");
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}/value", "GET", "getValue",
                                  "Get the attribute's value. This call is intended for use with attributes that have no data reference only. For attributes with a data reference, consult the documentation for Streams.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributes/{webId}", "DELETE", "delete",
                                  "Delete an attribute.");
        methods.insert(method);

        method = new WebApiMethod("/unitclasses/{webId}/units", "POST", "createUnit",
                                  "Create a unit in the specified Unit Class.");
        method.setBodyParameter("unitDTO", "The new unit definition..");
        methods.insert(method);

        method = new WebApiMethod("/unitclasses/{webId}", "GET", "get", "Retrieve a unit class.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/unitclasses/{webId}/units", "GET", "getUnits",
                                  "Get a list of all units belonging to the unit class.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/unitclasses/{webId}", "PATCH", "update",
                                  "Update a unit class.");
        method.setBodyParameter("unitClassDTO",
                                "A partial unit class containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/unitclasses", "GET", "getByPath",
                                  "Retrieve a unit class by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the unit class..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/unitclasses/{webId}/canonicalunit", "GET", "getCanonicalUnit",
                                  "Get the canonical unit of a unit class.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/unitclasses/{webId}", "DELETE", "delete",
                                  "Delete a unit class.");
        methods.insert(method);

        method = new WebApiMethod("/attributetraits", "GET", "getByCategory",
                                  "Retrieve all attribute traits of the specified category/categories.");
        method.addParameter(new UrlParameter("category", List.class,
                                             "The category of the attribute traits. Multiple categories may be specified with multiple instances of the parameter. If the parameter is not specified, or if its value is \"all\", then all attribute traits of all categories will be returned..",
                                             true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributetraits/{name}", "GET", "get",
                                  "Retrieve an attribute trait.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/dataservers/{webId}/enumerationsets", "GET",
                                  "getEnumerationSets",
                                  "Retrieve enumeration sets for given Data Server.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/dataservers/{webId}/points", "POST", "createPoint",
                                  "Create a point in the specified Data Server.");
        method.setBodyParameter("pointDTO", "The new point definition..");
        methods.insert(method);

        method = new WebApiMethod("/dataservers/{webId}", "GET", "get", "Retrieve a Data Server.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/dataservers/{webId}/points", "GET", "getPoints",
                                  "Retrieve a list of points on a specified Data Server.");
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "A query string for filtering by point name. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is '0'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/dataservers", "GET", "list",
                                  "Retrieve a list of Data Servers known to this service.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/dataservers", "GET", "getByPath",
                                  "Retrieve a Data Server by path.");
        method.addParameter(new UrlParameter("path", String.class,
                                             "The path to the server. Note that the path supplied to this method must be of the form '\\servername'..",
                                             true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/dataservers/{webId}/enumerationsets", "POST",
                                  "createEnumerationSet",
                                  "Create an enumeration set on the Data Server.");
        method.setBodyParameter("enumerationSet", "The new enumeration set definition..");
        methods.insert(method);

        method = new WebApiMethod("/dataservers", "GET", "getByName",
                                  "Retrieve a Data Server by name.");
        method.addParameter(
                new UrlParameter("name", String.class, "The name of the server..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the analysis template based on the specified criteria. By default, all security entries for this analysis template are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the analysis template for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the analysis template with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}", "GET", "get",
                                  "Retrieve an analysis template.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}/categories", "GET", "getCategories",
                                  "Get an analysis template's categories.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}", "PATCH", "update",
                                  "Update an analysis template by replacing items in its definition.");
        method.setBodyParameter("template",
                                "A partial analysis template containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the analysis template.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates", "POST", "createFromAnalysis",
                                  "Create an Analysis template based upon a specified Analysis.");
        method.addParameter(new UrlParameter("analysisWebId", String.class,
                                             "The ID of the Analysis, on which the template is created..",
                                             true));
        method.addParameter(new UrlParameter("name", String.class,
                                             "The name for the created template, which must be unique within the database's AnalysisTemplate collection. If the name ends with an asterisk (*), then a unique name will be generated based on the supplied name. The default is the specified Analysis' name suffixed with an asterisk (*)..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the analysis template.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates", "GET", "getByPath",
                                  "Retrieve an analysis template by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the analysis template..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the analysis template.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysistemplates/{webId}", "DELETE", "delete",
                                  "Delete an analysis template.");
        methods.insert(method);

        method = new WebApiMethod("/securitymappings/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the security mapping based on the specified criteria. By default, all security entries for this security mapping are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securitymappings/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the security mapping for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securitymappings/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the security mapping with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securitymappings/{webId}", "GET", "get",
                                  "Retrieve a security mapping.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securitymappings/{webId}", "PATCH", "update",
                                  "Update a security mapping by replacing items in its definition.");
        method.setBodyParameter("securityMapping",
                                "A partial security mapping containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/securitymappings", "GET", "getByPath",
                                  "Retrieve a security mapping by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the security mapping..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securitymappings/{webId}", "DELETE", "delete",
                                  "Delete a security mapping.");
        methods.insert(method);

        method = new WebApiMethod("/analysisrules/{webId}", "GET", "get",
                                  "Retrieve an Analysis Rule.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysisrules/{webId}", "PATCH", "update",
                                  "Update an Analysis Rule by replacing items in its definition.");
        method.setBodyParameter("analysisRule",
                                "A partial Analysis Rule containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/analysisrules", "GET", "getByPath",
                                  "Retrieve an Analysis Rule by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the Analysis Rule..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysisrules/{webId}/analysisrules", "POST",
                                  "createAnalysisRule",
                                  "Create a new Analysis Rule as a child of an existing Analysis Rule.");
        method.setBodyParameter("analysisRule", "The definition of the new Analysis Rule..");
        methods.insert(method);

        method = new WebApiMethod("/analysisrules/{webId}/analysisrules", "GET", "getAnalysisRules",
                                  "Get the child Analysis Rules of the Analysis Rule.");
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding Analysis Rules. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include Analysis Rules nested further than the immediate children of the searchRoot. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysisrules/{webId}", "DELETE", "delete",
                                  "Delete an Analysis Rule.");
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/securityentries", "GET", "getSecurityEntries",
                                  "Retrieve the security entries associated with the table based on the specified criteria. By default, all security entries for this table are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the table for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the table with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}", "GET", "get", "Retrieve a table.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the table.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/categories", "GET", "getCategories",
                                  "Get a table's categories.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}", "PATCH", "update",
                                  "Update a table by replacing items in its definition.");
        method.setBodyParameter("table", "A partial table containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the table.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/data", "PUT", "updateData",
                                  "Update the table's data.");
        method.setBodyParameter("data", "The new table data definition..");
        methods.insert(method);

        method = new WebApiMethod("/tables", "GET", "getByPath", "Retrieve a table by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the table..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/data", "GET", "getData",
                                  "Get the table's data.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}/securityentries", "POST", "createSecurityEntry",
                                  "Create a security entry owned by the table.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tables/{webId}", "DELETE", "delete", "Delete a table.");
        methods.insert(method);

        method = new WebApiMethod("/points/{webId}", "GET", "get", "Get a point.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/points/multiple", "GET", "getMultiple",
                                  "Retrieve multiple points by web id or path.");
        method.addParameter(new UrlParameter("asParallel", Boolean.class,
                                             "Specifies if the retrieval processes should be run in parallel on the server. This may improve the response time for large amounts of requested points. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("includeMode", String.class,
                                             "The include mode for the return list. The default is 'All'..",
                                             false));
        method.addParameter(new UrlParameter("path", List.class,
                                             "The path of a point. Multiple points may be specified with multiple instances of the parameter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("webId", List.class,
                                             "The ID of a point. Multiple points may be specified with multiple instances of the parameter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/points/{webId}/attributes/{name}", "PUT",
                                  "updateAttributeValue", "Update a point attribute value.");
        method.setBodyParameter("value", "The new value of the attribute to be updated..");
        methods.insert(method);

        method = new WebApiMethod("/points/{webId}", "PATCH", "update", "Update a point.");
        method.setBodyParameter("pointDTO", "A partial point containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/points", "GET", "getByPath", "Get a point by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the point..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/points/{webId}/attributes", "GET", "getAttributes",
                                  "Get point attributes.");
        method.addParameter(new UrlParameter("name", List.class,
                                             "The name of a point attribute to be returned. Multiple attributes may be specified with multiple instances of the parameter..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The filter to the names of the list of point attributes to be returned. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/points/{webId}/attributes/{name}", "GET", "getAttributeByName",
                                  "Get a point attribute by name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/points/{webId}", "DELETE", "delete", "Delete a point.");
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the table category based on the specified criteria. By default, all security entries for this table category are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the table category for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the table category with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}", "GET", "get",
                                  "Retrieve a table category.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}", "PATCH", "update",
                                  "Update a table category by replacing items in its definition.");
        method.setBodyParameter("tableCategory",
                                "A partial table category containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/tablecategories", "GET", "getByPath",
                                  "Retrieve a table category by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the target table category..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the table category.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the table category.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the table category.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/tablecategories/{webId}", "DELETE", "delete",
                                  "Delete a table category.");
        methods.insert(method);

        method = new WebApiMethod("/timeruleplugins/{webId}", "GET", "get",
                                  "Retrieve a Time Rule Plug-in.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/timeruleplugins", "GET", "getByPath",
                                  "Retrieve a Time Rule Plug-in by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the Time Rule Plug-in..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}/securityentries", "GET", "getSecurityEntries",
                                  "Retrieve the security entries associated with the analysis based on the specified criteria. By default, all security entries for this analysis are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the Analysis for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the analysis with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}", "GET", "get", "Retrieve an Analysis.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}/categories", "GET", "getCategories",
                                  "Get an Analysis' categories.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}", "PATCH", "update", "Update an Analysis.");
        method.setBodyParameter("analysis", "A partial Analysis containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the analysis.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the analysis.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses", "GET", "getByPath", "Retrieve an Analysis by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the Analysis..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the analysis.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analyses/{webId}", "DELETE", "delete", "Delete an Analysis.");
        methods.insert(method);

        method = new WebApiMethod("/analysisruleplugins/{webId}", "GET", "get",
                                  "Retrieve an Analysis Rule Plug-in.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysisruleplugins", "GET", "getByPath",
                                  "Retrieve an Analysis Rule Plug-in by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the Analysis Rule Plug-in..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/eventframes", "POST", "createEventFrame",
                                  "Create an event frame as a child of the specified event frame.");
        method.setBodyParameter("eventFrame", "The new event frame definition..");
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the event frame for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/referencedelements", "GET",
                                  "getReferencedElements",
                                  "Retrieve the event frame's referenced elements.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/categories", "GET", "getCategories",
                                  "Get an event frame's categories.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/annotations/{id}", "DELETE",
                                  "deleteAnnotation", "Delete an annotation on an event frame.");
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/attributes", "POST", "createAttribute",
                                  "Create a new attribute of the specified event frame.");
        method.setBodyParameter("attribute", "The definition of the new attribute..");
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/annotations/{id}", "GET",
                                  "getAnnotationById",
                                  "Get a specific annotation on an event frame.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/acknowledge", "PATCH", "acknowledge",
                                  "Calls the EventFrame's Acknowledge method.");
        methods.insert(method);

        method = new WebApiMethod("/eventframes/multiple", "GET", "getMultiple",
                                  "Retrieve multiple event frames by web ids or paths.");
        method.addParameter(new UrlParameter("asParallel", Boolean.class,
                                             "Specifies if the retrieval processes should be run in parallel on the server. This may improve the response time for large amounts of requested attributes. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("includeMode", String.class,
                                             "The include mode for the return list. The default is 'All'..",
                                             false));
        method.addParameter(new UrlParameter("path", List.class,
                                             "The path of an event frame. Multiple event frames may be specified with multiple instances of the parameter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("webId", List.class,
                                             "The ID of an event frame. Multiple event frames may be specified with multiple instances of the parameter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/searchbyattribute", "POST",
                                  "createSearchByAttribute",
                                  "Create a link for a \"Search EventFrames By Attribute Value\" operation, whose queries are specified in the request content. The SearchRoot is specified by the Web Id of the root EventFrame. If the SearchRoot is not specified, then the search starts at the Asset Database. ElementTemplate must be provided as the Web ID of the ElementTemplate, which are used to create the EventFrames. All the attributes in the queries must be defined as AttributeTemplates on the ElementTemplate. An array of attribute value queries are ANDed together to find the desired Element objects. At least one value query must be specified. There are limitations on SearchOperators.");
        method.setBodyParameter("search", ".");
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/attributes/capture", "POST",
                                  "captureValues", "Calls the EventFrame's CaptureValues method.");
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/config", "POST", "createConfig",
                                  "Executes the create configuration function of the data references found within the attributes of the event frame, and optionally, its children.");
        method.addParameter(new UrlParameter("includeChildElements", Boolean.class,
                                             "If true, includes the child event frames of the specified event frame..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/eventframes", "GET", "getEventFrames",
                                  "Retrieve event frames based on the specified conditions. By default, returns all children of the specified root event frame with a start time in the past 8 hours.");
        method.addParameter(new UrlParameter("canBeAcknowledged", Boolean.class,
                                             "Specify the returned event frames' canBeAcknowledged property. The default is no canBeAcknowledged filter..",
                                             false));
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned event frames must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "The ending time for the search. The endTime must be greater than or equal to the startTime. The searchMode parameter will control whether the comparison will be performed against the event frame's startTime or endTime. The default is '*' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("isAcknowledged", Boolean.class,
                                             "Specify the returned event frames' isAcknowledged property. The default no isAcknowledged filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding event frames. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("referencedElementNameFilter", String.class,
                                             "The name query string which must match the name of a referenced element. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("referencedElementTemplateName", String.class,
                                             "Specify that returned event frames must have an element in the event frame's referenced elements collection that derives from the template. Specify this parameter by name..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies whether the search should include objects nested further than the immediate children of the search root. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("searchMode", String.class,
                                             "Determines how the startTime and endTime parameters are treated when searching for event frame objects to be included in the returned collection. If this parameter is one of the 'Backward*' or 'Forward*' values, none of endTime, sortField, or sortOrder may be specified. The default is 'Overlapped'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("severity", List.class,
                                             "Specify that returned event frames must have this severity. Multiple severity values may be specified with multiple instances of the parameter. The default is no severity filter..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "The starting time for the search. startTime must be less than or equal to the endTime. The searchMode parameter will control whether the comparison will be performed against the event frame's startTime or endTime. The default is '*-8h'..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned event frames must have this template or a template derived from this template. The default is no template filter. Specify this parameter by name..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the event frame based on the specified criteria. By default, all security entries for this event frame are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}", "GET", "get", "Retrieve an event frame.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/searchbyattribute/{searchId}", "GET",
                                  "executeSearchByAttribute",
                                  "Execute a \"Search EventFrames By Attribute Value\" operation.");
        method.addParameter(new UrlParameter("canBeAcknowledged", Boolean.class,
                                             "Specify the returned event frames' canBeAcknowledged property. The default is no canBeAcknowledged filter..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "The ending time for the search. endTime must be greater than or equal to the startTime. The searchMode parameter will control whether the comparison will be performed against the event frame's startTime or endTime. The default is '*'..",
                                             false));
        method.addParameter(new UrlParameter("isAcknowledged", Boolean.class,
                                             "Specify the returned event frames' isAcknowledged property. The default no isAcknowledged filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding event frames. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("referencedElementNameFilter", String.class,
                                             "The name query string which must match the name of a referenced element. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies whether the search should include objects nested further than the immediate children of the search root. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("searchMode", String.class,
                                             "Determines how the startTime and endTime parameters are treated when searching for event frame objects to be included in the returned collection. The default is 'Overlapped'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("severity", List.class,
                                             "Specify that returned event frames must have this severity. Multiple severity values may be specified with multiple instances of the parameter. The default is no severity filter..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "The starting time for the search. startTime must be less than or equal to the endTime. The searchMode parameter will control whether the comparison will be performed against the event frame's startTime or endTime. The default is '*-8h'..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}", "PATCH", "update",
                                  "Update an event frame by replacing items in its definition.");
        method.setBodyParameter("eventFrame",
                                "A partial event frame containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/eventframes", "GET", "getByPath",
                                  "Retrieve an event frame by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the event frame..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the event frame.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the event frame.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/eventframeattributes", "GET",
                                  "findEventFrameAttributes",
                                  "Retrieves a list of event frame attributes matching the specified filters from the specified event frame.");
        method.addParameter(new UrlParameter("attributeCategory", String.class,
                                             "Specify that returned attributes must have this category. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeDescriptionFilter", String.class,
                                             "The attribute description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeNameFilter", String.class,
                                             "The attribute name filter string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeType", String.class,
                                             "Specify that returned attributes' value type must be this value type. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "A string representing the latest ending time for the event frames to be matched. The endTime must be greater than or equal to the startTime. The default is '*'..",
                                             false));
        method.addParameter(new UrlParameter("eventFrameCategory", String.class,
                                             "Specify that the owner of the returned attributes must have this category. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("eventFrameDescriptionFilter", String.class,
                                             "The event frame description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("eventFrameNameFilter", String.class,
                                             "The event frame name filter string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("eventFrameTemplate", String.class,
                                             "Specify that the owner of the returned attributes must have this template or a template derived from this template. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned (the page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("referencedElementNameFilter", String.class,
                                             "The name query string which must match the name of a referenced element. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include objects nested further than immediate children of the given resource. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("searchMode", String.class,
                                             "Determines how the startTime and endTime parameters are treated when searching for event frames.     The default is 'Overlapped'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "A string representing the earliest starting time for the event frames to be matched. startTime must be less than or equal to the endTime. The default is '*-8h'..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/annotations", "GET", "getAnnotations",
                                  "Get an event frame's annotations.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the event frame with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/annotations", "POST", "createAnnotation",
                                  "Create an annotation on an event frame.");
        method.setBodyParameter("annotation", "The new annotation definition..");
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/attributes", "GET", "getAttributes",
                                  "Get the attributes of the specified event frame.");
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned attributes must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding attributes. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class,
                                             "Specified if the search should include attributes with the Excluded property set. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class,
                                             "Specified if the search should include attributes with the Hidden property set. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned attributes must be members of this template. The default is no template filter..",
                                             false));
        method.addParameter(new UrlParameter("valueType", String.class,
                                             "Specify that returned attributes' value type must be the given value type. The default is no value type filter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/annotations/{id}", "PATCH",
                                  "updateAnnotation",
                                  "Update an annotation on an event frame by replacing items in its definition.");
        method.setBodyParameter("annotation",
                                "A partial annotation containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the event frame.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/eventframes/{webId}", "DELETE", "delete",
                                  "Delete an event frame.");
        methods.insert(method);

        method = new WebApiMethod("/enumerationvalues/{webId}", "DELETE", "deleteEnumerationValue",
                                  "Delete an enumeration value from an enumeration set.");
        methods.insert(method);

        method = new WebApiMethod("/enumerationvalues/{webId}", "GET", "get",
                                  "Retrieve an enumeration value mapping");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationvalues/{webId}", "PATCH", "updateEnumerationValue",
                                  "Update an enumeration value by replacing items in its definition.");
        method.setBodyParameter("enumerationValue",
                                "A partial enumeration value containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/enumerationvalues", "GET", "getByPath",
                                  "Retrieve an enumeration value by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the target enumeration value..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securityidentities/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the security identity based on the specified criteria. By default, all security entries for this security identity are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securityidentities/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the security identity for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securityidentities/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the security identity with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securityidentities/{webId}", "GET", "get",
                                  "Retrieve a security identity.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securityidentities/{webId}", "PATCH", "update",
                                  "Update a security identity by replacing items in its definition.");
        method.setBodyParameter("securityIdentity",
                                "A partial security identity containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/securityidentities", "GET", "getByPath",
                                  "Retrieve a security identity by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the security identity..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securityidentities/{webId}/securitymappings", "GET",
                                  "getSecurityMappings",
                                  "Get security mappings for the specified security identity.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/securityidentities/{webId}", "DELETE", "delete",
                                  "Delete a security identity.");
        methods.insert(method);

        method = new WebApiMethod("/system/cacheinstances", "GET", "cacheInstances",
                                  "Get AF cache instances currently in use by the system. These are caches from which user requests are serviced. The number of instances depends on the number of users connected to the service, the service's authentication method, and the cache instance configuration.");
        methods.insert(method);

        method = new WebApiMethod("/system/status", "GET", "status",
                                  "Get the system uptime, the system state and the number of cache instances for this PI System Web API instance.");
        methods.insert(method);

        method = new WebApiMethod("/system/userinfo", "GET", "userInfo",
                                  "Get information about the Windows identity used to fulfill the request. This depends on the service's authentication method and the credentials passed by the client. The impersonation level of the Windows identity is included.");
        methods.insert(method);

        method = new WebApiMethod("/system", "GET", "landing",
                                  "Get system links for this PI System Web API instance.");
        methods.insert(method);

        method = new WebApiMethod("/system/versions", "GET", "versions",
                                  "Get the current versions of the PI Web API instance and all external plugins.");
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/attributes", "POST", "createAttribute",
                                  "Create a new attribute of the specified element.");
        method.setBodyParameter("attribute", "The definition of the new attribute..");
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the element for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/referencedelements", "GET",
                                  "getReferencedElements",
                                  "Retrieve referenced elements based on the specified conditions. By default, this method selects all referenced elements of the current resource.");
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned elements must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("descriptionFilter", String.class,
                                             "Specify that returned elements must have this description. The default is no description filter..",
                                             false));
        method.addParameter(new UrlParameter("elementType", String.class,
                                             "Specify that returned elements must have this type. The default type is 'Any'..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned elements must have this template or a template derived from this template. The default is no template filter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/categories", "GET", "getCategories",
                                  "Get an element's categories.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/elementattributes", "GET",
                                  "findElementAttributes",
                                  "Retrieves a list of element attributes matching the specified filters from the specified element.");
        method.addParameter(new UrlParameter("attributeCategory", String.class,
                                             "Specify that returned attributes must have this category. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeDescriptionFilter", String.class,
                                             "The attribute description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeNameFilter", String.class,
                                             "The attribute name filter string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("attributeType", String.class,
                                             "Specify that returned attributes' value type must be this value type. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementCategory", String.class,
                                             "Specify that the owner of the returned attributes must have this category. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementDescriptionFilter", String.class,
                                             "The element description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementNameFilter", String.class,
                                             "The element name filter string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementTemplate", String.class,
                                             "Specify that the owner of the returned attributes must have this template or a template derived from this template. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("elementType", String.class,
                                             "Specify that the element of the returned attributes must have this AFElementType. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned (the page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include objects nested further than immediate children of the given resource. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/multiple", "GET", "getMultiple",
                                  "Retrieve multiple elements by web id or path.");
        method.addParameter(new UrlParameter("asParallel", Boolean.class,
                                             "Specifies if the retrieval processes should be run in parallel on the server. This may improve the response time for large amounts of requested attributes. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("includeMode", String.class,
                                             "The include mode for the return list. The default is 'All'..",
                                             false));
        method.addParameter(new UrlParameter("path", List.class,
                                             "The path of an element. Multiple elements may be specified with multiple instances of the parameter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("webId", List.class,
                                             "The ID of an element. Multiple elements may be specified with multiple instances of the parameter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/analyses", "POST", "createAnalysis",
                                  "Create an Analysis.");
        method.setBodyParameter("analysis", "The new Analysis definition..");
        methods.insert(method);

        method = new WebApiMethod("/elements/searchbyattribute", "POST", "createSearchByAttribute",
                                  "Create a link for a \"Search Elements By Attribute Value\" operation, whose queries are specified in the request content. The SearchRoot is specified by the Web Id of the root Element. If the SearchRoot is not specified, then the search starts at the Asset Database. ElementTemplate must be provided as the Web ID of the ElementTemplate, which are used to create the Elements. All the attributes in the queries must be defined as AttributeTemplates on the ElementTemplate. An array of attribute value queries are ANDed together to find the desired Element objects. At least one value query must be specified. There are limitations on SearchOperators.");
        method.setBodyParameter("search", ".");
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/config", "POST", "createConfig",
                                  "Executes the create configuration function of the data references found within the attributes of the element, and optionally, its children.");
        method.addParameter(new UrlParameter("includeChildElements", Boolean.class,
                                             "If true, includes the child elements of the specified element..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/eventframes", "GET", "getEventFrames",
                                  "Retrieve event frames that reference this element based on the specified conditions. By default, returns all event frames that reference this element with a start time in the past 8 hours.");
        method.addParameter(new UrlParameter("canBeAcknowledged", Boolean.class,
                                             "Specify the returned event frames' canBeAcknowledged property. The default is no canBeAcknowledged filter..",
                                             false));
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned event frames must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "The ending time for the search. The endTime must be greater than or equal to the startTime. The searchMode parameter will control whether the comparison will be performed against the event frame's startTime or endTime. The default is '*' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("isAcknowledged", Boolean.class,
                                             "Specify the returned event frames' isAcknowledged property. The default no isAcknowledged filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding event frames. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchMode", String.class,
                                             "Determines how the startTime and endTime parameters are treated when searching for event frame objects to be included in the returned collection. If this parameter is one of the 'Backward*' or 'Forward*' values, none of endTime, sortField, or sortOrder may be specified. The default is 'Overlapped'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("severity", List.class,
                                             "Specify that returned event frames must have this severity. Multiple severity values may be specified with multiple instances of the parameter. The default is no severity filter..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending' if searchMode is not one of the 'Backward*' or 'Forward*' values..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "The starting time for the search. startTime must be less than or equal to the endTime. The searchMode parameter will control whether the comparison will be performed against the event frame's startTime or endTime. The default is '*-8h'..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned event frames must have this template or a template derived from this template. The default is no template filter. Specify this parameter by name..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/securityentries", "GET", "getSecurityEntries",
                                  "Retrieve the security entries associated with the element based on the specified criteria. By default, all security entries for this element are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}", "GET", "get", "Retrieve an element.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}", "PATCH", "update",
                                  "Update an element by replacing items in its definition.");
        method.setBodyParameter("element", "A partial element containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/elements", "GET", "getByPath", "Retrieve an element by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the element..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the element.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/analyses", "GET", "getAnalyses",
                                  "Retrieve analyses based on the specified conditions.");
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/elements", "POST", "createElement",
                                  "Create a child element.");
        method.setBodyParameter("element", "The new element definition..");
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the element.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/searchbyattribute/{searchId}", "GET",
                                  "executeSearchByAttribute",
                                  "Execute a \"Search Elements By Attribute Value\" operation.");
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that the owner of the returned attributes must have this category. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("descriptionFilter", String.class,
                                             "The element description filter string used for finding objects. Only the first 440 characters of the description will be searched. For Asset Servers older than 2.7, a 400 status code (Bad Request) will be returned if this parameter is specified. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned. The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include objects nested further than the immediate children of the searchRoot. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/referencedelements", "POST",
                                  "addReferencedElement",
                                  "Add a reference to an existing element to the child elements collection.");
        method.addParameter(new UrlParameter("referencedElementWebId", List.class,
                                             "The ID of the referenced element. Multiple referenced elements may be specified with multiple instances of the parameter..",
                                             true));
        method.addParameter(new UrlParameter("referenceType", String.class,
                                             "The name of the reference type between the parent and the referenced element. The default is \"parent-child\"..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the element with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/referencedelements", "DELETE",
                                  "removeReferencedElement",
                                  "Remove a reference to an existing element from the child elements collection.");
        method.addParameter(new UrlParameter("referencedElementWebId", List.class,
                                             "The ID of the referenced element. Multiple referenced elements may be specified with multiple instances of the parameter..",
                                             true));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/attributes", "GET", "getAttributes",
                                  "Get the attributes of the specified element.");
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned attributes must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding attributes. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class,
                                             "Specified if the search should include attributes with the Excluded property set. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class,
                                             "Specified if the search should include attributes with the Hidden property set. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned attributes must be members of this template. The default is no template filter..",
                                             false));
        method.addParameter(new UrlParameter("valueType", String.class,
                                             "Specify that returned attributes' value type must be the given value type. The default is no value type filter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/elements", "GET", "getElements",
                                  "Retrieve elements based on the specified conditions. By default, this method selects immediate children of the specified element.");
        method.addParameter(new UrlParameter("categoryName", String.class,
                                             "Specify that returned elements must have this category. The default is no category filter..",
                                             false));
        method.addParameter(new UrlParameter("descriptionFilter", String.class,
                                             "Specify that returned elements must have this description. The default is no description filter..",
                                             false));
        method.addParameter(new UrlParameter("elementType", String.class,
                                             "Specify that returned elements must have this type. The default type is 'Any'..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned per call (page size). The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for finding objects. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class,
                                             "Specifies if the search should include objects nested further than the immediate children of the searchRoot. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("startIndex", Integer.class,
                                             "The starting index (zero based) of the items to be returned. The default is 0..",
                                             false));
        method.addParameter(new UrlParameter("templateName", String.class,
                                             "Specify that returned elements must have this template or a template derived from this template. The default is no template filter..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the element.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elements/{webId}", "DELETE", "delete", "Delete an element.");
        methods.insert(method);

        /*
        method = new WebApiMethod("/streamsets/{webId}/recorded", "GET", "getRecorded", "Returns recorded values of the attributes for an element, event frame, or attribute.");
        method.addParameter(new UrlParameter("boundaryType", String.class, "An optional value that determines how the times and values of the returned end points are determined. The default is 'Inside'..", false));
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("endTime", String.class, "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..", false));
        method.addParameter(new UrlParameter("filterExpression", String.class, "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. The default is no filtering..", false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class, "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..", false));
        method.addParameter(new UrlParameter("maxCount", Integer.class, "The maximum number of values to be returned. The default is 1000..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("startTime", String.class, "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/value", "GET", "getValues", "Returns values of the attributes for an Element, Event Frame or Attribute at the specified time.");
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        method.addParameter(new UrlParameter("time", String.class, "An AF time string, which is used as the time context to get stream values if it is provided. By default, it is not specified, and the default time context of the AF object will be used..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/value", "POST", "updateValue", "Updates a single value for the specified streams.");
        method.setBodyParameter("values", "The values to add or update..");
        method.addParameter(new UrlParameter("bufferOption", String.class, "The desired AFBufferOption. The default is 'BufferIfPossible'..", false));
        method.addParameter(new UrlParameter("updateOption", String.class, "The desired AFUpdateOption. The default is 'Replace'..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/interpolatedattimes", "GET", "getInterpolatedAtTimes", "Returns interpolated values of attributes for an element, event frame or attribute at the specified times.");
        method.addParameter(new UrlParameter("time", List.class, "The timestamp at which to retrieve a interpolated value. Multiple timestamps may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("filterExpression", String.class, "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. If the attribute does not support filtering, the filter will be ignored. The default is no filtering..", false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class, "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("sortOrder", String.class, "The order that the returned collection is sorted. The default is 'Ascending'..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/plot", "GET", "getPlotAdHoc", "Returns values of attributes for the specified streams over the specified time range suitable for plotting over the number of intervals (typically represents pixels).");
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream.  Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("endTime", String.class, "An optional end time. The default is '*'. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..", false));
        method.addParameter(new UrlParameter("intervals", Integer.class, "The number of intervals to plot over. Typically, this would be the number of horizontal pixels in the trend. The default is '24'. For each interval, the data available is examined and significant values are returned. Each interval can produce up to 5 values if they are unique, the first value in the interval, the last value, the highest value, the lowest value and at most one exceptional point (bad status or digital state)..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("startTime", String.class, "An optional start time. The default is '*-1d'..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/value", "POST", "updateValueAdHoc", "Updates a single value for the specified streams.");
        method.setBodyParameter("values", "The values to add or update..");
        method.addParameter(new UrlParameter("bufferOption", String.class, "The desired AFBufferOption. The default is 'BufferIfPossible'..", false));
        method.addParameter(new UrlParameter("updateOption", String.class, "The desired AFUpdateOption. The default is 'Replace'..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/recordedattimes", "GET", "getRecordedAtTimesAdHoc", "Returns recorded values of the specified streams at the specified times.");
        method.addParameter(new UrlParameter("time", List.class, "The timestamp at which to retrieve a recorded value. Multiple timestamps may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream. Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("retrievalMode", String.class, "An optional value that determines the values to return when values don't exist at the exact time specified. The default is 'Auto'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("sortOrder", String.class, "The order that the returned collection is sorted. The default is 'Ascending'..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/interpolatedattimes", "GET", "getInterpolatedAtTimesAdHoc", "Returns interpolated values of the specified streams at the specified times.");
        method.addParameter(new UrlParameter("time", List.class, "The timestamp at which to retrieve a interpolated value. Multiple timestamps may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream. Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("filterExpression", String.class, "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. If the attribute does not support filtering, the filter will be ignored. The default is no filtering..", false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class, "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("sortOrder", String.class, "The order that the returned collection is sorted. The default is 'Ascending'..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/interpolated", "GET", "getInterpolated", "Returns interpolated values of attributes for an element, event frame or attribute over the specified time range at the specified sampling interval.");
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("endTime", String.class, "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..", false));
        method.addParameter(new UrlParameter("filterExpression", String.class, "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. If the attribute does not support filtering, the filter will be ignored. The default is no filtering..", false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class, "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..", false));
        method.addParameter(new UrlParameter("interval", String.class, "The sampling interval, in AFTimeSpan format..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("startTime", String.class, "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/recorded", "POST", "updateValues", "Updates multiple values for the specified streams.");
        method.setBodyParameter("values", "The values to add or update..");
        method.addParameter(new UrlParameter("bufferOption", String.class, "The desired AFBufferOption. The default is 'BufferIfPossible'..", false));
        method.addParameter(new UrlParameter("updateOption", String.class, "The desired AFUpdateOption. The default is 'Replace'..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/recorded", "GET", "getRecordedAdHoc", "Returns recorded values of the specified streams.");
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream.  Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("boundaryType", String.class, "An optional value that determines how the times and values of the returned end points are determined. The default is 'Inside'..", false));
        method.addParameter(new UrlParameter("endTime", String.class, "An optional end time. The default is '*'. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..", false));
        method.addParameter(new UrlParameter("filterExpression", String.class, "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. The default is no filtering..", false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class, "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..", false));
        method.addParameter(new UrlParameter("maxCount", Integer.class, "The maximum number of values to be returned. The default is 1000..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("startTime", String.class, "An optional start time. The default is '*-1d'..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/summary", "GET", "getSummaries", "Returns summary values of the attributes for an element, event frame or attribute.");
        method.addParameter(new UrlParameter("calculationBasis", String.class, "Specifies the method of evaluating the data over the time range. The default is 'TimeWeighted'..", false));
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("endTime", String.class, "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..", false));
        method.addParameter(new UrlParameter("filterExpression", String.class, "A string containing a filter expression. Expression variables are relative to the attribute. Use '.' to reference the containing attribute. The default is no filtering..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("sampleInterval", String.class, "A time span specifies how often the filter expression is evaluated when computing the summary for an interval, if the sampleType is 'Interval'..", false));
        method.addParameter(new UrlParameter("sampleType", String.class, "A flag which specifies one or more summaries to compute for each interval over the time range. The default is 'ExpressionRecordedValues'..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("startTime", String.class, "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..", false));
        method.addParameter(new UrlParameter("summaryDuration", String.class, "The duration of each summary interval..", false));
        method.addParameter(new UrlParameter("summaryType", List.class, "Specifies the kinds of summaries to produce over the range. The default is 'Total'. Multiple summary types may be specified by using multiple instances of summaryType..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        method.addParameter(new UrlParameter("timeType", String.class, "Specifies how to calculate the timestamp for each interval. The default is 'Auto'..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/recordedattime", "GET", "getRecordedAtTime", "Returns recorded values of the attributes for an element, event frame, or attribute.");
        method.addParameter(new UrlParameter("time", String.class, "The timestamp at which the values are desired..", true));
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("retrievalMode", String.class, "An optional value that determines the values to return when values don't exist at the exact time specified. The default is 'Auto'..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/channel", "GET", "getChannelAdHoc", "Opens a channel that will send messages about any value changes for the specified streams.");
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream.  Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("includeInitialValues", Boolean.class, "Specified if the channel should send a message with the current values of all the streams after the connection is opened. The default is 'false'..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/channel", "GET", "getChannel", "Opens a channel that will send messages about any value changes for the attributes of an Element, Event Frame, or Attribute.");
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("includeInitialValues", Boolean.class, "Specified if the channel should send a message with the current values of all the streams after the connection is opened. The default is 'false'..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/end", "GET", "getEndAdHoc", "Returns End Of Stream values for attributes of the specified streams");
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream.  Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/interpolated", "GET", "getInterpolatedAdHoc", "Returns interpolated values of the specified streams over the specified time range at the specified sampling interval.");
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream. Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("endTime", String.class, "An optional end time. The default is '*'. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..", false));
        method.addParameter(new UrlParameter("filterExpression", String.class, "An optional string containing a filter expression. Expression variables are relative to the data point. Use '.' to reference the containing attribute. If the attribute does not support filtering, the filter will be ignored. The default is no filtering..", false));
        method.addParameter(new UrlParameter("includeFilteredValues", Boolean.class, "Specify 'true' to indicate that values which fail the filter criteria are present in the returned data at the times where they occurred with a value set to a 'Filtered' enumeration value with bad status. Repeated consecutive failures are omitted..", false));
        method.addParameter(new UrlParameter("interval", String.class, "The sampling interval, in AFTimeSpan format..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("startTime", String.class, "An optional start time. The default is '*-1d'..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/value", "GET", "getValuesAdHoc", "Returns values of the specified streams.");
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream.  Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("time", String.class, "An AF time string, which is used as the time context to get stream values if it is provided. By default, it is not specified, and the default time context of the AF object will be used..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/recordedattimes", "GET", "getRecordedAtTimes", "Returns recorded values of attributes for an element, event frame or attribute at the specified times.");
        method.addParameter(new UrlParameter("time", List.class, "The timestamp at which to retrieve a recorded value. Multiple timestamps may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("retrievalMode", String.class, "An optional value that determines the values to return when values don't exist at the exact time specified. The default is 'Auto'..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("sortOrder", String.class, "The order that the returned collection is sorted. The default is 'Ascending'..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/plot", "GET", "getPlot", "Returns values of attributes for an element, event frame or attribute over the specified time range suitable for plotting over the number of intervals (typically represents pixels).");
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("endTime", String.class, "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..", false));
        method.addParameter(new UrlParameter("intervals", Integer.class, "The number of intervals to plot over. Typically, this would be the number of horizontal pixels in the trend. The default is '24'. For each interval, the data available is examined and significant values are returned. Each interval can produce up to 5 values if they are unique, the first value in the interval, the last value, the highest value, the lowest value and at most one exceptional point (bad status or digital state)..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("startTime", String.class, "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/recorded", "POST", "updateValuesAdHoc", "Updates multiple values for the specified streams.");
        method.setBodyParameter("values", "The values to add or update..");
        method.addParameter(new UrlParameter("bufferOption", String.class, "The desired AFBufferOption. The default is 'BufferIfPossible'..", false));
        method.addParameter(new UrlParameter("updateOption", String.class, "The desired AFUpdateOption. The default is 'Replace'..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/summary", "GET", "getSummariesAdHoc", "Returns summary values of the specified streams.");
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream.  Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("calculationBasis", String.class, "Specifies the method of evaluating the data over the time range. The default is 'TimeWeighted'..", false));
        method.addParameter(new UrlParameter("endTime", String.class, "An optional end time. The default is '*'. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..", false));
        method.addParameter(new UrlParameter("filterExpression", String.class, "A string containing a filter expression. Expression variables are relative to the attribute. Use '.' to reference the containing attribute. The default is no filtering..", false));
        method.addParameter(new UrlParameter("sampleInterval", String.class, "A time span specifies how often the filter expression is evaluated when computing the summary for an interval, if the sampleType is 'Interval'..", false));
        method.addParameter(new UrlParameter("sampleType", String.class, "A flag which specifies one or more summaries to compute for each interval over the time range. The default is 'ExpressionRecordedValues'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("startTime", String.class, "An optional start time. The default is '*-1d'..", false));
        method.addParameter(new UrlParameter("summaryDuration", String.class, "The duration of each summary interval..", false));
        method.addParameter(new UrlParameter("summaryType", List.class, "Specifies the kinds of summaries to produce over the range. The default is 'Total'. Multiple summary types may be specified by using multiple instances of summaryType..", false));
        method.addParameter(new UrlParameter("timeType", String.class, "Specifies how to calculate the timestamp for each interval. The default is 'Auto'..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/{webId}/end", "GET", "getEnd", "Returns End of stream values of the attributes for an Element, Event Frame or Attribute");
        method.addParameter(new UrlParameter("categoryName", String.class, "Specify that included attributes must have this category. The default is no category filter..", false));
        method.addParameter(new UrlParameter("nameFilter", String.class, "The name query string used for filtering attributes. The default is no filter..", false));
        method.addParameter(new UrlParameter("searchFullHierarchy", Boolean.class, "Specifies if the search should include attributes nested further than the immediate attributes of the searchRoot. The default is 'false'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("showExcluded", Boolean.class, "Specified if the search should include attributes with the Excluded property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("showHidden", Boolean.class, "Specified if the search should include attributes with the Hidden property set. The default is 'false'..", false));
        method.addParameter(new UrlParameter("templateName", String.class, "Specify that included attributes must be members of this template. The default is no template filter..", false));
        methods.insert(method);

        method = new WebApiMethod("/streamsets/recordedattime", "GET", "getRecordedAtTimeAdHoc", "Returns recorded values based on the passed time and retrieval mode.");
        method.addParameter(new UrlParameter("time", String.class, "The timestamp at which the values are desired..", true));
        method.addParameter(new UrlParameter("webId", List.class, "The ID of a stream.  Multiple streams may be specified with multiple instances of the parameter..", true));
        method.addParameter(new UrlParameter("retrievalMode", String.class, "An optional value that determines the values to return when values don't exist at the exact time specified. The default is 'Auto'..", false));
        method.addParameter(new UrlParameter("selectedFields", String.class, "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..", false));
        method.addParameter(new UrlParameter("timeZone", String.class, "The time zone in which the time string will be interpreted. This parameter will be ignored if a time zone is specified in the time string. If no time zone is specified in either places, the PI Web API server time zone will be used..", false));
        methods.insert(method);
        */

        method = new WebApiMethod("/", "GET", "get",
                                  "Get top level links for this PI System Web API instance.");
        methods.insert(method);

        method = new WebApiMethod("/timerules/{webId}", "DELETE", "delete", "Delete a Time Rule.");
        methods.insert(method);

        method = new WebApiMethod("/timerules/{webId}", "GET", "get", "Retrieve a Time Rule.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/timerules/{webId}", "PATCH", "update",
                                  "Update a Time Rule by replacing items in its definition.");
        method.setBodyParameter("timeRule", "A partial Time Rule containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/timerules", "GET", "getByPath",
                                  "Retrieve a Time Rule by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the Time Rule..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the element category based on the specified criteria. By default, all security entries for this element category are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the element category for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the element category with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}", "GET", "get",
                                  "Retrieve an element category.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}", "PATCH", "update",
                                  "Update an element category by replacing items in its definition.");
        method.setBodyParameter("elementCategory",
                                "A partial element category containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/elementcategories", "GET", "getByPath",
                                  "Retrieve an element category by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the target element category..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the element category.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the element category.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the element category.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/elementcategories/{webId}", "DELETE", "delete",
                                  "Delete an element category.");
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the analysis category based on the specified criteria. By default, all security entries for this analysis category are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the analysis category for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the analysis category with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}", "GET", "get",
                                  "Retrieve an analysis category.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}", "PATCH", "update",
                                  "Update an analysis category by replacing items in its definition.");
        method.setBodyParameter("category",
                                "A partial analysis category containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories", "GET", "getByPath",
                                  "Retrieve an analysis category by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the target analysis category..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the analysis category.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the analysis category.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the analysis category.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/analysiscategories/{webId}", "DELETE", "delete",
                                  "Delete an analysis category.");
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the attribute category based on the specified criteria. By default, all security entries for this attribute category are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the attribute category for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the attribute category with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}", "GET", "get",
                                  "Retrieve an attribute category.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}", "PATCH", "update",
                                  "Update an attribute category by replacing items in its definition.");
        method.setBodyParameter("category",
                                "A partial attribute category containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/attributecategories", "GET", "getByPath",
                                  "Retrieve an attribute category by path.");
        method.addParameter(new UrlParameter("path", String.class,
                                             "The path to the target attribute category..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the attribute category.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the attribute category.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the attribute category.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributecategories/{webId}", "DELETE", "delete",
                                  "Delete an attribute category.");
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/unitclasses", "POST", "createUnitClass",
                                  "Create a unit class in the specified Asset Server.");
        method.setBodyParameter("unitClass", "The new unit class definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries of the specified security item associated with the asset server based on the specified criteria. By default, all security entries for this asset server are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries information to be returned. If the parameter is not specified, security entries of the 'Default' security item will be returned..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the asset server for a specified user.");
        method.addParameter(new UrlParameter("securityItem", List.class,
                                             "The security item of the desired security information to be returned. Multiple security items may be specified with multiple instances of the parameter. If the parameter is not specified, only 'Default' security item of the security information will be returned..",
                                             true));
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry of the specified security item associated with the asset server with the specified name.");
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries information to be returned. If the parameter is not specified, security entries of the 'Default' security item will be returned..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}", "GET", "get",
                                  "Retrieve an Asset Server.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securitymappings", "POST",
                                  "createSecurityMapping", "Create a security mapping.");
        method.setBodyParameter("securityMapping", "The new security mapping definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetservers", "GET", "list",
                                  "Retrieve a list of all Asset Servers known to this service.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/analysisruleplugins", "GET",
                                  "getAnalysisRulePlugIns",
                                  "Retrieve a list of all Analysis Rule Plug-in's.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers", "GET", "getByPath",
                                  "Retrieve an Asset Server by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the server..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the asset server.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries to be updated. If the parameter is not specified, security entries of the 'Default' security item will be updated..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/timeruleplugins", "GET",
                                  "getTimeRulePlugIns",
                                  "Retrieve a list of all Time Rule Plug-in's.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/unitclasses", "GET", "getUnitClasses",
                                  "Retrieve a list of all unit classes on the specified Asset Server.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securityidentities", "GET",
                                  "getSecurityIdentitiesForUser",
                                  "Retrieve security identities for a specific user.");
        method.addParameter(
                new UrlParameter("userIdentity", String.class, "The user identity to search for..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securityidentities", "POST",
                                  "createSecurityIdentity", "Create a security identity.");
        method.setBodyParameter("securityIdentity", "The new security identity definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/assetdatabases", "POST",
                                  "createAssetDatabase", "Create an asset database.");
        method.setBodyParameter("database", "The new database definition..");
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securityidentities", "GET",
                                  "getSecurityIdentities",
                                  "Retrieve security identities based on the specified criteria. By default, all security identities in the specified Asset Server are returned.");
        method.addParameter(new UrlParameter("field", String.class,
                                             "Specifies which of the object's properties are searched. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned. The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("query", String.class,
                                             "The query string used for finding objects. The default is no query string..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the asset server.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries to be deleted. If the parameter is not specified, security entries of the 'Default' security item will be deleted..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers", "GET", "getByName",
                                  "Retrieve an Asset Server by name.");
        method.addParameter(
                new UrlParameter("name", String.class, "The name of the server..", true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securitymappings", "GET",
                                  "getSecurityMappings",
                                  "Retrieve security mappings based on the specified criteria. By default, all security mappings in the specified Asset Server are returned.");
        method.addParameter(new UrlParameter("field", String.class,
                                             "Specifies which of the object's properties are searched. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("maxCount", Integer.class,
                                             "The maximum number of objects to be returned. The default is 1000..",
                                             false));
        method.addParameter(new UrlParameter("query", String.class,
                                             "The query string used for finding objects. The default is no query string..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortField", String.class,
                                             "The field or property of the object used to sort the returned collection. The default is 'Name'..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the asset server.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        method.addParameter(new UrlParameter("securityItem", String.class,
                                             "The security item of the desired security entries to be created. If the parameter is not specified, security entries of the 'Default' security item will be created..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/assetservers/{webId}/assetdatabases", "GET", "getDatabases",
                                  "Retrieve a list of all Asset Databases on the specified Asset Server.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/system/configuration/{key}", "PUT", "put",
                                  "Create or update a configuration item.");
        method.setBodyParameter("value", "The value of the configuration item..");
        methods.insert(method);

        method = new WebApiMethod("/system/configuration/{key}", "DELETE", "delete",
                                  "Delete a configuration item.");
        methods.insert(method);

        method = new WebApiMethod("/system/configuration", "GET", "list",
                                  "Get the current system configuration.");
        methods.insert(method);

        method = new WebApiMethod("/system/configuration/{key}", "GET", "get",
                                  "Get the value of a configuration item.");
        methods.insert(method);

        /*
        method = new WebApiMethod("/channels/instances", "GET", "instances", "Retrieves a list of currently running channel instances.");
        methods.insert(method);
        */

        method = new WebApiMethod("/enumerationsets/{webId}/enumerationvalues", "POST",
                                  "createValue",
                                  "Create an enumeration value for a enumeration set.");
        method.setBodyParameter("enumerationValue", "The new enumeration value definition..");
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}/security", "GET", "getSecurity",
                                  "Get the security information of the specified security item associated with the enumeration set for a specified user.");
        method.addParameter(new UrlParameter("userIdentity", List.class,
                                             "The user identity for the security information to be checked. Multiple security identities may be specified with multiple instances of the parameter. If the parameter is not specified, only the current user's security rights will be returned..",
                                             true));
        method.addParameter(new UrlParameter("forceRefresh", Boolean.class,
                                             "Indicates if the security cache should be refreshed before getting security information. The default is 'false'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}/securityentries/{name}", "GET",
                                  "getSecurityEntryByName",
                                  "Retrieve the security entry associated with the enumeration set with the specified name.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}", "GET", "get",
                                  "Retrieve an enumeration set.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}", "PATCH", "update",
                                  "Update an enumeration set by replacing items in its definition.");
        method.setBodyParameter("enumerationSet",
                                "A partial enumeration set containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}/enumerationvalues", "GET", "getValues",
                                  "Retrieve an enumeration set's values.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}/securityentries/{name}", "PUT",
                                  "updateSecurityEntry",
                                  "Update a security entry owned by the enumeration set.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied or they will be removed..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}/securityentries", "GET",
                                  "getSecurityEntries",
                                  "Retrieve the security entries associated with the enumeration set based on the specified criteria. By default, all security entries for this enumeration set are returned.");
        method.addParameter(new UrlParameter("nameFilter", String.class,
                                             "The name query string used for filtering security entries. The default is no filter..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}/securityentries/{name}", "DELETE",
                                  "deleteSecurityEntry",
                                  "Delete a security entry owned by the enumeration set.");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets", "GET", "getByPath",
                                  "Retrieve an enumeration set by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the target enumeration set..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}/securityentries", "POST",
                                  "createSecurityEntry",
                                  "Create a security entry owned by the enumeration set.");
        method.setBodyParameter("securityEntry",
                                "The new security entry definition. The full list of allow and deny rights must be supplied..");
        method.addParameter(new UrlParameter("applyToChildren", Boolean.class,
                                             "If false, the new access permissions are only applied to the associated object. If true, the access permissions of children with any parent-child reference types will change when the permissions on the primary parent change..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/enumerationsets/{webId}", "DELETE", "delete",
                                  "Delete an enumeration set.");
        methods.insert(method);

        method = new WebApiMethod("/attributetemplates/{webId}/attributetemplates", "GET",
                                  "getAttributeTemplates",
                                  "Retrieve an attribute template's child attribute templates.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributetemplates/{webId}", "GET", "get",
                                  "Retrieve an attribute template.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributetemplates/{webId}", "PATCH", "update",
                                  "Update an existing attribute template by replacing items in its definition.");
        method.setBodyParameter("template",
                                "A partial attribute template containing the desired changes..");
        methods.insert(method);

        method = new WebApiMethod("/attributetemplates", "GET", "getByPath",
                                  "Retrieve an attribute template by path.");
        method.addParameter(
                new UrlParameter("path", String.class, "The path to the attribute template..",
                                 true));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributetemplates/{webId}/categories", "GET", "getCategories",
                                  "Get an attribute template's categories.");
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/attributetemplates/{webId}/attributetemplates", "POST",
                                  "createAttributeTemplate",
                                  "Create an attribute template as a child of another attribute template.");
        method.setBodyParameter("template", "The attribute template definition..");
        methods.insert(method);

        method = new WebApiMethod("/attributetemplates/{webId}", "DELETE", "delete",
                                  "Delete an attribute template.");
        methods.insert(method);

        method = new WebApiMethod("/calculation/intervals", "GET", "getAtIntervals",
                                  "Returns results of evaluating the expression over the time range from the start time to the end time at a defined interval.");
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..",
                                             false));
        method.addParameter(new UrlParameter("expression", String.class,
                                             "A string containing the expression to be evaluated. The syntax for the expression generally follows the Performance Equation syntax as described in the PI Server documentation, with the exception that expressions which target AF objects use attribute names in place of tag names in the equation..",
                                             false));
        method.addParameter(new UrlParameter("sampleInterval", String.class,
                                             "A time span specifies how often the filter expression is evaluated when computing the summary for an interval..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..",
                                             false));
        method.addParameter(new UrlParameter("webId", String.class,
                                             "The ID of the target object of the expression. A target object can be a Data Server, a database, an element, an event frame or an attribute. References to attributes or points are based on the target. If this parameter is not provided, the target object is set to null..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/calculation/summary", "GET", "getSummary",
                                  "Returns the result of evaluating the expression over the time range from the start time to the end time. The time range is first divided into a number of summary intervals. Then the calculation is performed for the specified summaries over each interval.");
        method.addParameter(new UrlParameter("calculationBasis", String.class,
                                             "Specifies the method of evaluating the data over the time range. The default is 'TimeWeighted'..",
                                             false));
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..",
                                             false));
        method.addParameter(new UrlParameter("expression", String.class,
                                             "A string containing the expression to be evaluated. The syntax for the expression generally follows the Performance Equation syntax as described in the PI Server documentation, with the exception that expressions which target AF objects use attribute names in place of tag names in the equation..",
                                             false));
        method.addParameter(new UrlParameter("sampleInterval", String.class,
                                             "A time span specifies how often the filter expression is evaluated when computing the summary for an interval, if the sampleType is 'Interval'..",
                                             false));
        method.addParameter(new UrlParameter("sampleType", String.class,
                                             "A flag which specifies one or more summaries to compute for each interval over the time range. The default is 'ExpressionRecordedValues'..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..",
                                             false));
        method.addParameter(new UrlParameter("summaryDuration", String.class,
                                             "The duration of each summary interval..", false));
        method.addParameter(new UrlParameter("summaryType", List.class,
                                             "Specifies the kinds of summaries to produce over the range. The default is 'Total'. Multiple summary types may be specified by using multiple instances of summaryType..",
                                             false));
        method.addParameter(new UrlParameter("timeType", String.class,
                                             "Specifies how to calculate the timestamp for each interval. The default is 'Auto'..",
                                             false));
        method.addParameter(new UrlParameter("webId", String.class,
                                             "The ID of the target object of the expression. A target object can be a Data Server, a database, an element, an event frame or an attribute. References to attributes or points are based on the target. If this parameter is not provided, the target object is set to null..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/calculation/recorded", "GET", "getAtRecorded",
                                  "Returns the result of evaluating the expression at each point in time over the time range from the start time to the end time where a recorded value exists for a member of the expression.");
        method.addParameter(new UrlParameter("endTime", String.class,
                                             "An optional end time. The default is '*' for element attributes and points. For event frame attributes, the default is the event frame's end time, or '*' if that is not set. Note that if endTime is earlier than startTime, the resulting values will be in time-descending order..",
                                             false));
        method.addParameter(new UrlParameter("expression", String.class,
                                             "A string containing the expression to be evaluated. The syntax for the expression generally follows the Performance Equation syntax as described in the PI Server documentation, with the exception that expressions which target AF objects use attribute names in place of tag names in the equation..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("startTime", String.class,
                                             "An optional start time. The default is '*-1d' for element attributes and points. For event frame attributes, the default is the event frame's start time, or '*-1d' if that is not set..",
                                             false));
        method.addParameter(new UrlParameter("webId", String.class,
                                             "The ID of the target object of the expression. A target object can be a Data Server, a database, an element, an event frame or an attribute. References to attributes or points are based on the target. If this parameter is not provided, the target object is set to null..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/calculation/times", "GET", "getAtTimes",
                                  "Returns the result of evaluating the expression at the specified timestamps.");
        method.addParameter(new UrlParameter("expression", String.class,
                                             "A string containing the expression to be evaluated. The syntax for the expression generally follows the Performance Equation syntax as described in the PI Server documentation, with the exception that expressions which target AF objects use attribute names in place of tag names in the equation..",
                                             false));
        method.addParameter(new UrlParameter("selectedFields", String.class,
                                             "List of fields to be returned in the response, separated by semicolons (;). If this parameter is not specified, all available fields will be returned..",
                                             false));
        method.addParameter(new UrlParameter("sortOrder", String.class,
                                             "The order that the returned collection is sorted. The default is 'Ascending'..",
                                             false));
        method.addParameter(new UrlParameter("time", List.class,
                                             "A list of timestamps at which to calculate the expression..",
                                             false));
        method.addParameter(new UrlParameter("webId", String.class,
                                             "The ID of the target object of the expression. A target object can be a Data Server, a database, an element, an event frame or an attribute. References to attributes or points are based on the target. If this parameter is not provided, the target object is set to null..",
                                             false));
        method.setStream(true);
        methods.insert(method);

        method = new WebApiMethod("/search/children", "GET", "getChildren",
                                  "Get child elements in a hierarchy.");
        method.addParameter(new UrlParameter("parent", String.class,
                                             "Parent element or database in the hierarchy. AF element is specified as af:\\\\server\\database\\element.",
                                             true));
        method.addParameter(new UrlParameter("fields", List.class,
                                             "List of fields to include in each Search Result. If no fields are specified, then the following fields are returned: afcategories; attributes; datatype; description; endtime; haschildren; itemtype; links; matchedfields; name; plottable; starttime; template; uniqueid; uom; webid. The following fields are not returned by default: paths; parents; explain (must be paired with the links field)",
                                             false));
        method.addParameter(new UrlParameter("count", Integer.class,
                                             "Max count of child objects to return (optional). Defaults to 10.",
                                             false));
        method.addParameter(new UrlParameter("start", Integer.class,
                                             "Starting result for this result set. Note that the first result is at position 0.",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/search/sources", "DELETE", "deleteSource",
                                  "Delete a database that has been previously indexed by the search service.");
        method.addParameter(new UrlParameter("name", String.class,
                                             "The name of the search source to delete, in the form of pi:piservername or af:\\\\afservername\\afdatabasename.",
                                             true));
        methods.insert(method);

        method = new WebApiMethod("/search/settings", "POST", "editSetting",
                                  "Edit settings. Note: This overwrites the previous setting, it does not append to it.");
        method.addParameter(
                new UrlParameter("scaninterval", Integer.class, "Scan Interval in seconds", true));
        method.addParameter(
                new UrlParameter("pointattributes", String.class, "'|' delimited point attributes",
                                 true));
        methods.insert(method);

        method = new WebApiMethod("/search/sources/crawl", "POST", "fullCrawl",
                                  "Initiate a full crawl of a source database with the supplied name.");
        method.addParameter(new UrlParameter("name", String.class,
                                             "The name of a search source to crawl, in the form of: pi:piservername or af:\\\\afservername\\afdatabasename. Otherwise a full crawl of all sources is triggered.",
                                             true));
        methods.insert(method);

        method = new WebApiMethod("/search/settings", "GET", "getSettings",
                                  "Get the crawler settings for the search service.");
        methods.insert(method);

        method = new WebApiMethod("/search/sources", "GET", "getSources",
                                  "Get a list of databases indexed by the search service.");
        method.addParameter(new UrlParameter("name", String.class,
                                             "JSON object with Name of the source, in the form of pi:piservername or af:\\\\afservername\\afdatabasename as well as CrawlerHost, the hostname where the crawler service is running. Defaults to the same machine as the search service.",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/search/version", "GET", "getVersion",
                                  "Get the version of the PI Web API Indexed Search service");
        methods.insert(method);

        method = new WebApiMethod("/search/serviceinfo", "GET", "getIndexInfo",
                                  "Get indexing information for current indexed sources of the PI Web API Indexed Search service.");
        methods.insert(method);

        method = new WebApiMethod("/search", "GET", "getLinks",
                                  "Get top level links for the PI Web API Indexed Search service.");
        methods.insert(method);

        method = new WebApiMethod("/search/metrics", "GET", "getMetrics",
                                  "Get query metrics for the PI Web API Indexed Search Service.");
        methods.insert(method);

        method = new WebApiMethod("/search/query", "GET", "query",
                                  "Query the crawled data by keyword(s).");
        method.addParameter(new UrlParameter("q", List.class,
                                             "One or many terms, in the form of field:value, like \"name:pump\". If no field is specfied, like \"pump\", then the following fields will all be used: name, description, afcategories, afelementtemplate, attributename, attributedescription. The star and question mark wildcards are supported, for example: boil* or boi?er. To perform a fuzzy search, append a tilde to the end of a keyword, like \"boilr~\" will match \"boiler\". If multiple terms are entered, they are assumed to be ORed together. If that's not appropriate, you can specify AND, OR, and NOT operators, along with parenthesis to create a complex query. For example \"(vibration* AND datatype:float32) OR afelementtemplate:pump\" Special characters are used through the keyword syntax, so those characters must be escaped if they are in a literal search term. The following characters must be escaped with a backslash: + - && || ! ( ) { } [ ] ^ \" ~ * ? : \\ For example, to find a PI point named SI:NUSO.ID specify \"q=name:SI\\:USO.ID\"",
                                             false));
        method.addParameter(new UrlParameter("scope", List.class,
                                             "List of sources to execute the query against. Specify the sources in string format (e.g. pi:mypidataarchive) or in webId format. Multiple scopes (and with different formats) can be specified, separated by semicolons (;).",
                                             false));
        method.addParameter(new UrlParameter("fields", List.class,
                                             "List of fields to include in each Search Result. If no fields are specified, then the following fields are returned: afcategory; attributes; datatype; description; endtime; haschildren; itemtype; links; matchedfields; name; plottable; starttime; template; uniqueid; uom; webid. The following fields are not returned by default: paths; parents; explain (must be paired with the links field)",
                                             false));
        method.addParameter(new UrlParameter("count", Integer.class,
                                             "Max number of results to return. The default is 10 per page.",
                                             false));
        method.addParameter(new UrlParameter("start", Integer.class,
                                             "Index of search result to begin with. The default is to start at index 0.",
                                             false));
        methods.insert(method);

        method = new WebApiMethod("/search/sources", "POST", "createSource",
                                  "Create a new search source");
        method.setBodyParameter("source",
                                "JSON object with Name of the source, in the form of pi:piservername or af:\\\\afservername\\afdatabasename as well as CrawlerHost, the hostname where the crawler service is running. Defaults to the same machine as the search service.");
        methods.insert(method);

        method = new WebApiMethod("/search/sources", "PUT", "updateSource", "Edit a search source");
        method.setBodyParameter("source", "The updated search source object");
        methods.insert(method);
    }

}
