package com.changheng.accountant.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.changheng.accountant.AppContext;
import com.changheng.accountant.R;
import com.changheng.accountant.importdao.PlanDao;
import com.changheng.accountant.entity.AppUpdate;
import com.changheng.accountant.entity.Area;
import com.changheng.accountant.entity.Chapter;
import com.changheng.accountant.entity.Course;
import com.changheng.accountant.entity.CourseList;
import com.changheng.accountant.entity.ExamQuestion;
import com.changheng.accountant.entity.ExamRule;
import com.changheng.accountant.entity.KnowledgeList;
import com.changheng.accountant.entity.Paper;
import com.changheng.accountant.entity.PaperList;
import com.changheng.accountant.entity.UpdateDataEntity;
import com.changheng.accountant.importdao.CourseDao;
import com.changheng.accountant.importdao.CourseDao2;
import com.changheng.accountant.importdao.PaperDao;
import com.changheng.accountant.importdao.PaperDao2;
import com.changheng.accountant.importdao.PlanDao2;
import com.changheng.accountant.util.ApiClient;
import com.changheng.accountant.util.AreaUtils;
import com.changheng.accountant.util.CompressUtil;
import com.changheng.accountant.util.XMLParseUtil;
import com.changheng.accountant.util.XmlCreator;

public class ImportDataActivity extends BaseActivity implements OnClickListener {
	private AppContext appContext;
	private ProgressDialog proDialog;
	private Handler handler;
	private ArrayList<Area> areaList;
	
	//SD卡中数据库压缩文件保存目录  /mnt/sdcard/kuaiji/zipfiles/
	private static final String zipBaseDir = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator
			+ "kuaiji" + File.separator + "zipfiles" + File.separator;
	//SD卡中数据库文件保存目录  /mnt/sdcard/kuaiji/database/
	private static final String dbBaseDir = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator
			+ "kuaiji" + File.separator + "database" + File.separator;
	//SD卡中数据更新文件保存目录  /mnt/sdcard/kuaiji/updateData/
	private static final String UPDATAPATH = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator+"CHAccountant"+File.separator+"kuaiji"+File.separator+"updateData"+File.separator;
	//SD卡中数据更新XML保存目录  /mnt/sdcard/kuaiji/updateXml/
	private static final String XMLPATH = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator+"CHAccountant"+File.separator+"kuaiji"+File.separator+"updateXml"+File.separator;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_import_data);
		this.findViewById(R.id.importData).setOnClickListener(this);
		this.findViewById(R.id.importPlan).setOnClickListener(this);
		this.findViewById(R.id.updateData).setOnClickListener(this);
		this.findViewById(R.id.turnData).setOnClickListener(this);
		this.findViewById(R.id.importAllData).setOnClickListener(this);
		this.findViewById(R.id.importAllPlan).setOnClickListener(this);
		this.findViewById(R.id.getAllUpdata).setOnClickListener(this);
		this.findViewById(R.id.test).setOnClickListener(this);
		appContext = (AppContext) getApplication();
		handler = new Handler() {
			public void handleMessage(android.os.Message msg) {
				if (proDialog != null) {
					proDialog.dismiss();
				}
				switch (msg.what) {
				case 1:
					print("导入计划数据成功");
					break;
				case -1:
					print("导入计划数据失败");
					break;
				case 2:
					print("导入题目数据成功");
					break;
				case -2:
					print("导入题目数据失败");
					break;
				case 3:
					print("更新题目数据成功");
					break;
				case -3:
					print("更新题目数据失败");
					break;
				case 4:
					print("转换题目数据成功");
					break;
				case -4:
					print("转换题目数据失败");
					break;
				case 5:
					print("导入各个地区的题目数据成功");
					break;
				case -5:
					print("导入各个地区的题目数据失败");
					break;
				case 6:
					print("导入各个地区的计划数据成功");
					break;
				case -6:
					print("导入各个地区的计划数据失败");
					break;
				case 7:
					print("获取各个地区的更新数据成功");
					break;
				case -7:
					print("获取各个地区的更新数据失败");
					break;
				}
			};
		};
	}

	private void print(String s) {
		Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.importPlan:
			importPlan();
			break;
		case R.id.importData:
			importData();
			break;
		case R.id.updateData:
			updateData();
			break;
		case R.id.turnData:
			turnData();
			break;
		case R.id.importAllData:
			importAllData();
			break;
		case R.id.importAllPlan:
			importAllPlan();
			break;
		case R.id.getAllUpdata:
			getAllUpdata();
			break;
		case R.id.test:
			test();
			break;
		}
	}
	//导入全国地区的数据，数据存于内部存储中
	private void importPlan() {
		if (proDialog == null) {
			proDialog = ProgressDialog.show(this, null, "导入数据中请稍候...", true,
					true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			proDialog.show();
		}
		new Thread() {
			public void run() {
				try {
					PlanDao dao = new PlanDao(ImportDataActivity.this); // 带 1
																		// 个参数的是自己创建数据库
					// 导入课程
					ArrayList<Course> result = XMLParseUtil.parseClass(
							ApiClient.getCourseData(appContext,46)).getClassList();
					dao.insertClass(result);
					// 导入地区
					dao.insertArea(ApiClient.getArea(appContext));
					// 导入章节详细
					dao.insertKnowledgeList(ApiClient.getKnowledge(appContext));
					// 分课程，分地区的计划
					for (Course c : result) {
						dao.insertPlans(ApiClient.getPlan(appContext,
								c.getCourseId(), AreaUtils.areaCode + "")); // 2是科目，13是地区
					}
					handler.sendEmptyMessage(1);
				} catch (Exception e) {
					handler.sendEmptyMessage(-1);
					e.printStackTrace();
				}
			};
		}.start();
	}

	private void importData() {
		if (proDialog == null) {
			proDialog = ProgressDialog.show(this, null, "导入数据中请稍候...", true,
					true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			proDialog.show();
		}
		new Thread() {
			public void run() {
				try {
					CourseDao courseDao = new CourseDao(ImportDataActivity.this); // 带
																					// 1
																					// 个参数的是自己创建数据库
					PaperDao dao = new PaperDao(ImportDataActivity.this);
					// 导入课程【科目以及下面的所有章节】
					CourseList result = XMLParseUtil.parseClass(ApiClient
							.getCourseData(appContext,46));
					ArrayList<Course> classList = result.getClassList();
					ArrayList<Chapter> chapterList = result.getChapterList();
					courseDao.save(classList, chapterList);
					// 导入章节题目
					for (int i = 0; i < classList.size(); i++) {
						Course c = classList.get(i);
						for (int j = 0; j < chapterList.size(); j++) {
							Chapter ch = chapterList.get(j);
							// 该课程下的所有节
							if (c.getCourseId().equals(ch.getClassId())
									&& (!ch.getPid().equals("0"))) {
								// 插入 知识点和章节试卷
								String zuheid = c.getCourseId() + "_"
										+ ch.getChapterId();
								KnowledgeList list = XMLParseUtil
										.parseKnowledge(ApiClient
												.getKnowledgeList(appContext,
														ch.getChapterId()), ch
												.getTitle(), zuheid);
								dao.insertKnowledge(list);
								// 插入节点的题目数据
								ArrayList<ExamQuestion> questionList = XMLParseUtil
										.parseQuestionList(ApiClient
												.getChapterQuestionList(
														appContext,
														ch.getChapterId(), "",46));
								ArrayList<ExamRule> ruleList = (ArrayList<ExamRule>) getRuleList(
										questionList, zuheid);
								dao.insertQuestions(questionList);
								dao.insertRules(ruleList, zuheid);
							}
						}
						// 插入试卷
						PaperList list1 = XMLParseUtil.parsePaperList(ApiClient
								.getPaperListData(
										(AppContext) getApplication(),
										c.getCourseId(), null));
						dao.insertPaperList(list1.getPaperlist());
						// 插入试卷的大题，以及题目
						for (Paper p : list1.getPaperlist()) {
							Paper parsePaper = XMLParseUtil
									.parsePaper(ApiClient.getPaperDetail(
											appContext, p.getPaperId()));
							ArrayList<ExamQuestion> questionList = XMLParseUtil
									.parseQuestionList(ApiClient
											.getQuestionList(appContext,
													p.getPaperId()));
							ArrayList<ExamRule> ruleList = parsePaper
									.getRuleList();
							dao.insertRules(ruleList, p.getPaperId());
							dao.insertQuestions(questionList);
						}
					}
					// 导入数据库添加时间
					dao.insertAddTime();
					handler.sendEmptyMessage(2);
				} catch (Exception e) {
					handler.sendEmptyMessage(-2);
					e.printStackTrace();
				}
			};

			private List<ExamRule> getRuleList(List<ExamQuestion> questionList,
					String zuheid) {
				List<ExamRule> list = new ArrayList<ExamRule>();
				StringBuffer buf = new StringBuffer();
				// 按题型的1,2,3,4来分
				for (int i = 1; i < 5; i++) {
					for (ExamQuestion q : questionList) {

						if (String.valueOf(i).equals(q.getQType())) {
							buf.append(q.getQid()).append(",");
						}
					}
					if (buf.length() > 0) {
						ExamRule r = new ExamRule();
						r.setRuleId(zuheid + "_" + i);
						r.setRuleTitle(getTitleStr(i));
						r.setPaperId(zuheid);
						r.setOrderInPaper(i);
						r.setContainQids(buf.substring(0, buf.lastIndexOf(",")));
						buf.delete(0, buf.length());
						list.add(r);
					}
				}
				return list;
			}

			private String getTitleStr(int i) {
				return i == 1 ? "单选题" : i == 2 ? "多选题" : i == 3 ? "不定项"
						: i == 4 ? "判断题" : "综合题";
			}
		}.start();
	}

	private void updateData() {
		if (proDialog == null) {
			proDialog = ProgressDialog.show(this, null, "导入数据中请稍候...", true,
					true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			proDialog.show();
		}
		new Thread() {
			public void run() {
				try {
					// UpdateDataEntity entity = XMLParseUtil.parseTest();
					UpdateDataEntity entity = (UpdateDataEntity) outputObject(Environment
							.getExternalStorageDirectory().getPath()
							+ "/CHAccountant/update_data.data");
					com.changheng.accountant.dao.PaperDao dao = new com.changheng.accountant.dao.PaperDao(
							ImportDataActivity.this);
					dao.updateDataBase(entity.getPaperList(),
							entity.getRuleList(), entity.getQList());
					handler.sendEmptyMessage(3);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(-3);
				}
			}
		}.start();
	}

	private void turnData() {
		if (proDialog == null) {
			proDialog = ProgressDialog.show(this, null, "导入数据中请稍候...", true,
					true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			proDialog.show();
		}
		final String date = getSharedPreferences("first_pref", 0).getString(
				"DBAddTime", "");
		new Thread() {
			public void run() {
				try {
					UpdateDataEntity entity = XMLParseUtil.parseTest(ApiClient
							.getUpdateData(appContext,
									date.replaceAll(" ", "%20"),AreaUtils.areaCode));
					inputObject(entity, Environment
							.getExternalStorageDirectory().getPath()
							+ File.separator+"CHAccountant"+File.separator,
							 "update_data" + AreaUtils.areaCode + ".zip");
					handler.sendEmptyMessage(4);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(-4);
				}
			}
		}.start();
	}

	private void inputObject(Serializable ser, String filePath,String fileName) throws Exception {
		File f = new File(filePath);
		if(!f.exists())
		{
			f.mkdirs();
		}
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = new FileOutputStream(new File(filePath+fileName));
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
		} catch (Exception e) {
			throw e;
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	private Object outputObject(String file) throws Exception {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = new FileInputStream(file);
			ois = new ObjectInputStream(fis);
			return (Serializable) ois.readObject();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
			// 反序列化失败 - 删除缓存文件
			if (e instanceof InvalidClassException) {
				// File data = getFileStreamPath(file);
				File data = new File(file);
				data.delete();
			}
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
			}
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

	private void importAllData() {
		if (proDialog == null) {
			proDialog = ProgressDialog.show(this, null, "导入数据中请稍候...", true,
					true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			proDialog.show();
		}
		new Thread() {
			public void run() {
				try {
					if (areaList == null) {
//						areaList = XMLParseUtil.parseArea(getAssets().open(
//								"other/questions.xml"));
						areaList = new ArrayList<Area>();
						areaList.add(new Area(46,"CHINA","cn"));
					}
					for (Area area : areaList) {
						String fileName = "accountant" + area.getId() + ".db";
						CourseDao2 courseDao = new CourseDao2(
								ImportDataActivity.this, area.getId()); // 带
						// 1
						// 个参数的是自己创建数据库
						PaperDao2 dao = new PaperDao2(ImportDataActivity.this,
								area.getId());

						// 导入课程【科目以及下面的所有章节】
						CourseList result = XMLParseUtil.parseClass(ApiClient
								.getCourseData(appContext,area.getId()));
						ArrayList<Course> classList = result.getClassList();
						ArrayList<Chapter> chapterList = result
								.getChapterList();
						courseDao.save(classList, chapterList);
						// 导入章节题目
						for (int i = 0; i < classList.size(); i++) {
							Course c = classList.get(i);
							for (int j = 0; j < chapterList.size(); j++) {
								Chapter ch = chapterList.get(j);
								// 该课程下的所有节
								if (c.getCourseId().equals(ch.getClassId())
										&& (!ch.getPid().equals("0"))) {
									// 插入 知识点和章节试卷
									String zuheid = c.getCourseId() + "_"
											+ ch.getChapterId();
									KnowledgeList list = XMLParseUtil
											.parseKnowledge(ApiClient
													.getKnowledgeList(
															appContext,
															ch.getChapterId()),
													ch.getTitle(), zuheid);
									dao.insertKnowledge(list);
									// 插入节点的题目数据
									ArrayList<ExamQuestion> questionList = XMLParseUtil
											.parseQuestionList(ApiClient
													.getChapterQuestionList(
															appContext,
															ch.getChapterId(),
															"",area.getId()));
									ArrayList<ExamRule> ruleList = (ArrayList<ExamRule>) getRuleList(
											questionList, zuheid);
									dao.insertQuestions(questionList);
									dao.insertRules(ruleList, zuheid);
								}
							}
							// 插入试卷
							PaperList list1 = XMLParseUtil
									.parsePaperList(ApiClient.getPaperListData(
											(AppContext) getApplication(),
											c.getCourseId(), null));
							dao.insertPaperList(list1.getPaperlist());
							// 插入试卷的大题，以及题目
							for (Paper p : list1.getPaperlist()) {
								Paper parsePaper = XMLParseUtil
										.parsePaper(ApiClient.getPaperDetail(
												appContext, p.getPaperId()));
								ArrayList<ExamQuestion> questionList = XMLParseUtil
										.parseQuestionList(ApiClient
												.getQuestionList(appContext,
														p.getPaperId()));
								ArrayList<ExamRule> ruleList = parsePaper
										.getRuleList();
								dao.insertRules(ruleList, p.getPaperId());
								dao.insertQuestions(questionList);
							}
						}
						// 导入数据库添加时间
						dao.insertAddTime();
						// 打包压缩
						if(!new File(zipBaseDir).exists())
						{
							new File(zipBaseDir).mkdirs();
						}
						CompressUtil.zipFile(dbBaseDir, fileName, zipBaseDir
								+ "accountant" + area.getId() + ".zip");
					}
					// 所有地区数据都搞定后
					handler.sendEmptyMessage(5);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(-5);
				}
			};

			private List<ExamRule> getRuleList(List<ExamQuestion> questionList,
					String zuheid) {
				List<ExamRule> list = new ArrayList<ExamRule>();
				StringBuffer buf = new StringBuffer();
				// 按题型的1,2,3,4来分
				for (int i = 1; i < 5; i++) {
					for (ExamQuestion q : questionList) {

						if (String.valueOf(i).equals(q.getQType())) {
							buf.append(q.getQid()).append(",");
						}
					}
					if (buf.length() > 0) {
						ExamRule r = new ExamRule();
						r.setRuleId(zuheid + "_" + i);
						r.setRuleTitle(getTitleStr(i));
						r.setPaperId(zuheid);
						r.setOrderInPaper(i);
						r.setContainQids(buf.substring(0, buf.lastIndexOf(",")));
						buf.delete(0, buf.length());
						list.add(r);
					}
				}
				return list;
			}

			private String getTitleStr(int i) {
				return i == 1 ? "单选题" : i == 2 ? "多选题" : i == 3 ? "不定项"
						: i == 4 ? "判断题" : "综合题";
			}
		}.start();
	}

	private void importAllPlan() {
		if (proDialog == null) {
			proDialog = ProgressDialog.show(this, null, "导入数据中请稍候...", true,
					true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			proDialog.show();
		}
		new Thread() {
			public void run() {
				try {
					if (areaList == null) {
						areaList = XMLParseUtil.parseArea(getAssets().open(
								"other/questions.xml"));
					}
					for (Area area : areaList) {
						String fileName = "study_plan"+area.getId()+".db";
						PlanDao2 dao = new PlanDao2(area.getId()); // 带
																			// 1
						// 个参数的是自己创建数据库
						// 导入课程
						ArrayList<Course> result = XMLParseUtil.parseClass(
								ApiClient.getCourseData(appContext,area.getId()))
								.getClassList();
						dao.insertClass(result);
						// 导入地区
						dao.insertArea(ApiClient.getArea(appContext));
						// 导入章节详细
						dao.insertKnowledgeList(ApiClient
								.getKnowledge(appContext));
						// 分课程，分地区的计划
						for (Course c : result) {
							dao.insertPlans(ApiClient.getPlan(appContext,
									c.getCourseId(), AreaUtils.areaCode + "")); // 2是科目，13是地区
						}
						//压缩成zip
						if(!new File(zipBaseDir).exists())
						{
							new File(zipBaseDir).mkdirs();
						}
						CompressUtil.zipFile(dbBaseDir, fileName, zipBaseDir+"study_plan"+area.getId()+".zip");
					}
					handler.sendEmptyMessage(6);
				} catch (Exception e) {
					e.printStackTrace();
					handler.sendEmptyMessage(-6);
				}
			};
		}.start();
	}

	private void getAllUpdata()
	{
		if (proDialog == null) {
			proDialog = ProgressDialog.show(this, null, "导入数据中请稍候...", true,
					true);
			proDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		} else {
			proDialog.show();
		}
		final String date = getSharedPreferences("first_pref", 0).getString(
				"DBAddTime", "");
		new Thread() {
			public void run() {
				try {
					if (areaList == null) {
						areaList = XMLParseUtil.parseArea(getAssets().open(
								"other/area.xml"));
					}
					for (Area area : areaList) {
						UpdateDataEntity entity = XMLParseUtil.parseTest(ApiClient
								.getUpdateData(appContext,
										date.replaceAll(" ", "%20"),area.getId()));
						String fileName = "update_data" + area.getId() + ".zip";
						String fileName2 = "update_data" + area.getId() + ".xml";
						inputObject(entity, UPDATAPATH,fileName);
						//生成XML文件
						AppUpdate update = new AppUpdate("http://www.cyedu.org/UserCenter/mobile/"+fileName, "题库更新", (int) new File(UPDATAPATH+fileName).length(), 2, "1.0");
						update.setAddTime(entity.getDataAddTime());
						XmlCreator.WriteFileData(XMLPATH, fileName2, XmlCreator.WriteXmlStr(update));
					}
					handler.sendEmptyMessage(7);
				}catch(Exception e)
				{
					e.printStackTrace();
					handler.sendEmptyMessage(-7);
				}
			}
		}.start();
	}
	private void test()
	{
		try{
			KnowledgeList list = XMLParseUtil
					.parseKnowledge(ApiClient
							.getKnowledgeList(appContext,
									"46"), "","");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
