package com.csrd.pims.dao.entity.nce;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * 预制点
 */
@Data
public class HuaweiPresetPosition {

    private String id;

    private List<PositionInfo> positionInfos = new LinkedList<>();

    @Data
    public static class PositionInfo {
        // 预制点起始位（包含）
        private int beginDistance;
        // 预制点中止位（不包含）
        private int endDistance;
        // 预制点
        private int presetPosition;
    }
}
