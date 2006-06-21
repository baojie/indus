package edu.iastate.anthill.indus.agent;

public interface IndusCommand
{
    // 2004-10 commands for C/S communication
    public static final String CMD_HELLO               = "Hello";

    public static final String CMD_NEW_TYPE            = "NewType";

    public static final String CMD_GET_ALL_TYPE        = "GetAllType";

    public static final String CMD_GET_ALL_SCHEMA      = "GetAllSchema";

    public static final String CMD_GET_ALL_MAPPING     = "GetAllMapping";

    public static final String CMD_DELETE_TYPE         = "DeleteType";

    public static final String CMD_DELETE_SCHEMA       = "DeleteSchema";

    public static final String CMD_DELETE_MAPPING      = "DeleteMapping";

    public static final String CMD_GET_TYPE_DETAILS    = "GetTypeDetails";

    public static final String CMD_GET_SCHEMA_DETAILS  = "GetSchemaDetails";

    public static final String CMD_GET_MAPPING_DETAILS = "GetMappingDetails";

    public static final String CMD_UPDATE_TYPE         = "UpdateType";

    public static final String CMD_UPDATE_SCHEMA       = "UpdateSchema";

    public static final String CMD_UPDATE_MAPPING      = "UpdateMapping";

    // 2005-03-23 commands for view
    public static final String CMD_GET_ALL_VIEW        = "GetAllView";

    public static final String CMD_DELETE_VIEW         = "DeleteView";

    public static final String CMD_GET_VIEW_DETAILS    = "GetViewDetails";

    public static final String CMD_UPDATE_VIEW         = "UpdateView";

    // response for C/S communication
    public static final String RES_GENERAL_ERROR       = "GeneralError";

    public static final String RES_OK                  = "OK";

    public static final String RES_UNKNOWN_CMD         = "UnknownCommand";
}
