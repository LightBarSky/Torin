package com.torin.dbService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BatchResultDto {
    private Integer rowsUpdateOrInsert = 0;
    private Integer rowsNotAffected = 0;
    private Integer rowsUnknow = 0;
    private Integer rowsFailed = 0;

    public void update(int[] res) {
        for (int r : res) {
            if (r > 0) {
                rowsUpdateOrInsert++;
            }
            else if (r == 0) {
                rowsNotAffected++;
            }
            else if (r == java.sql.Statement.SUCCESS_NO_INFO) {
                rowsUnknow++;
            }
            else if (r == java.sql.Statement.EXECUTE_FAILED) {
                rowsFailed++;
            }
        }
    }
}
