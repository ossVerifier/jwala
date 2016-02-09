package com.siemens.cto.aem.common.exec;

/**
 * {@link CommandOutput} return code enum.
 *
 * Created by JC043760 on 2/9/2016.
 */
public enum CommandOutputReturnCode {

    SUCCESS(0, "Successful"),
    FAILED(1, "Failed"),
    NO_SUCH_SERVICE(123, "No such service"),
    TIMED_OUT(124, "Timed out"),
    ABNORMAL_SUCCESS(126, "Service already started/stopped"),
    NO_OP(127, "No operation"),
    KILL(255, "Kill executed"),
    UNKNOWN(99999, "Return code not in enum")
    ;

    private final int retCode;
    private final String desc;

    CommandOutputReturnCode(final int retCode, final String desc) {
        this.retCode = retCode;
        this.desc = desc;
    }

    public int getCodeNumber() {
        return retCode;
    }

    public String getDesc() {
        return desc;
    }

    public static CommandOutputReturnCode fromReturnCode(final int retCode) {
        for (CommandOutputReturnCode commandOutputReturnCode: CommandOutputReturnCode.values()) {
            if (commandOutputReturnCode.retCode == retCode) {
                return commandOutputReturnCode;
            }
        }
        return UNKNOWN;
    }

}
