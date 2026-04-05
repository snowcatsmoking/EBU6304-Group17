package data;

import ZiqianCao.java.TAJob;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 职位数据管理类。
 * 负责 TAJob 对象的增删改查，数据存储在 resources/Data/JobData/ 目录下。
 * 文件命名规则：{jobId}.json
 *
 * 使用方：
 *   - MO 端：发布/编辑/下架职位
 *   - TA 端：读取所有可申请职位
 *   - Admin 端：查看/删除任意职位
 */
public class JobDataManager {

    private final ObjectMapper objectMapper;

    public JobDataManager() {
        objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DataConfig.ensureDir(DataConfig.JOB_DIR);
    }

    // ==================== 写操作 ====================

    /**
     * 保存或更新一个职位。
     * 若 jobId 对应文件已存在则覆盖（更新），否则新建（发布）。
     *
     * @param job 要保存的 TAJob 对象，jobId 不能为空
     * @throws IllegalArgumentException jobId 为空时抛出
     * @throws IOException 文件读写失败时抛出
     */
    public void saveJob(TAJob job) throws IOException {
        if (job.getJobId() == null || job.getJobId().trim().isEmpty()) {
            throw new IllegalArgumentException("jobId 不能为空");
        }
        File file = new File(DataConfig.JOB_DIR + job.getJobId() + ".json");
        objectMapper.writeValue(file, job);
    }

    /**
     * 删除指定职位。
     *
     * @param jobId 职位 ID
     * @return true=删除成功，false=文件不存在
     */
    public boolean deleteJob(String jobId) {
        File file = new File(DataConfig.JOB_DIR + jobId + ".json");
        return file.exists() && file.delete();
    }

    /**
     * 下架职位（将 isActive 设为 true，isActive=true 表示已截止/不可申请）。
     * 不删除数据，仅改变状态。
     *
     * @param jobId 职位 ID
     * @return true=操作成功，false=职位不存在
     */
    public boolean deactivateJob(String jobId) {
        TAJob job = getJobById(jobId);
        if (job == null) return false;
        job.setActive(true);
        try {
            saveJob(job);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ==================== 读操作 ====================

    /**
     * 获取所有职位（包括已下架的）。
     * TA 端职位列表从这里读取，替代原来的硬编码。
     */
    public List<TAJob> getAllJobs() {
        List<TAJob> jobs = new ArrayList<>();
        File dir = new File(DataConfig.JOB_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return jobs;
        for (File file : files) {
            try {
                TAJob job = objectMapper.readValue(file, TAJob.class);
                jobs.add(job);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jobs;
    }

    /**
     * 获取所有仍可申请的职位（isActive=false）。
     * TA 端职位列表只显示这部分。
     */
    public List<TAJob> getActiveJobs() {
        List<TAJob> result = new ArrayList<>();
        for (TAJob job : getAllJobs()) {
            if (!job.isActive()) {
                result.add(job);
            }
        }
        return result;
    }

    /**
     * 获取指定 MO 发布的所有职位。
     * MO 端"我发布的职位"列表从这里读取。
     *
     * @param moStaffId MO 工号
     */
    public List<TAJob> getJobsByMo(String moStaffId) {
        List<TAJob> result = new ArrayList<>();
        for (TAJob job : getAllJobs()) {
            if (moStaffId.equals(job.getMoStaffId())) {
                result.add(job);
            }
        }
        return result;
    }

    /**
     * 按 jobId 查询单个职位。
     *
     * @param jobId 职位 ID
     * @return 对应的 TAJob，不存在则返回 null
     */
    public TAJob getJobById(String jobId) {
        File file = new File(DataConfig.JOB_DIR + jobId + ".json");
        if (!file.exists()) return null;
        try {
            return objectMapper.readValue(file, TAJob.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
