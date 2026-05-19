# ShiyangXie MO Feature Verification Notes

This note maps the new MO recruitment requirements to the implementation and gives a concise manual verification checklist.

## Requirement Coverage

| Requirement | Implementation Evidence | Manual Check |
|---|---|---|
| Set position skill requirements | MO post/edit forms provide preset skill checkboxes plus custom skill input; jobs persist `requiredSkills`; TA list, favorites, application dialog, MO details, statistics, and CSV export display skills. | Post a position with `Python`, `Java`, and one custom skill. Reopen edit page and confirm the same skills are selected and editable. |
| Auto close after deadline | `JobDataManager` refreshes expired jobs on reads; TA list uses `getActiveJobs`; application form reloads the latest job and blocks closed or expired jobs before saving. | Create or edit a job with a past deadline in data, open the TA position list, and confirm it is hidden or blocked from application. |
| Basic MO permission control | `JobDataManager` provides MO ownership checks; `TAApplicationRecordManager` provides MO-scoped review and keyword update methods; MO detail/review pages refuse cross-MO access. | Try to open or review an application whose `moStaffId` differs from the current MO and confirm the action is denied. |
| Intelligent applicant skill screening | MO review list calculates match scores with `SkillMatcher`, shows matched/missing skills, and supports sorting by match score, application date, status, or name/student ID. | Set required skills on a job, submit applications with different skills, then confirm higher-match candidates appear first under `Match Score`. |
| Resume keyword extraction | TA application form accepts resume/supporting text; `ResumeKeywordExtractor` stores automatic keywords; MO detail page allows save verified keywords or re-extract automatic keywords. | Submit resume text containing `Python`, `Machine Learning`, and TA experience, then confirm keywords appear in MO review detail and can be edited. |
