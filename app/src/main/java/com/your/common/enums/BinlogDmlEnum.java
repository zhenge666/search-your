package  com.your.common.enums;

import lombok.Getter;

/**
 * 数据操作类型
 *
 * @author zhangzhen
 * @Date 2022/4/16 下午4:30
 */
@Getter
public enum BinlogDmlEnum {

    /**
     *
     */
    INSERT(1, "INSERT"),
    UPDATE(2, "UPDATE"),
    DELETE(3, "DELETE");

    private Integer code;
    private String value;

    BinlogDmlEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public static BinlogDmlEnum getByValue(String value) {
        for (BinlogDmlEnum typeEnum : values()) {
            if (typeEnum.getValue().equalsIgnoreCase(value)) {
                return typeEnum;
            }
        }
        return null;
    }

}
