package jmetal.nichao.testreferences;

import jmetal.nichao.testreferences.core.BaseClassiferEnum;
import jmetal.nichao.testreferences.core.ClassifierValidation;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class CheckOnTest {


    /**
     * 将源项目分成70%的训练集和30%的验证集，对两者进行特征过滤，
     * 在70%训练数据上进行预测，然后在30%测试数据上进行判断，得到预测结果；
     *
     * @param trainning         训练数据的实例
     * @param testing           测试数据的实例
     * @param baseClassiferEnum 基分类器枚举
     * @return 返回Evaluation：表示预测得到的结果
     */
    public static Evaluation doValidation(Instances trainning, Instances testing, BaseClassiferEnum baseClassiferEnum, int[] toDeletedFeature) {
        Evaluation evaluations = null;
        try {
            trainning.setClassIndex(trainning.numAttributes() - 1);
            testing.setClassIndex(testing.numAttributes() - 1);

            Remove remove = new Remove();
            remove.setAttributeIndicesArray(toDeletedFeature);
            remove.setInputFormat(trainning);
            //Filter默认会产生新的Instances因此需要将trainning和testing指向新产生的Instances
            trainning = Filter.useFilter(trainning, remove);

            //删除训练集中的属性
            testing = Filter.useFilter(testing, remove);

            trainning.setClassIndex(trainning.numAttributes() - 1);
            testing.setClassIndex(testing.numAttributes() - 1);

            //ClassifierValidation类完成在训练集上构建模型，在测试集上进行预测的计算
            evaluations = ClassifierValidation.classify(trainning, testing, baseClassiferEnum, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return evaluations;
    }


    /**
     * 完成内层十次循环
     *
     * @param fileName       文件名称
     * @param outerInstances 被外层随机化后的项目实例
     * @param outerIterate   外层循环的编号
     */

    public static void doDefectPrediciton(String fileName, Instances outerInstances, int outerIterate) {
        //计算前70%的实例的索引
        int index = (int) Math.round(outerInstances.numInstances() * 0.7);
        Instances train = new Instances(outerInstances, 0, index);
        Instances test = new Instances(outerInstances, index, outerInstances.numInstances() - index);
        train.setClassIndex(train.numAttributes() - 1);
        test.setClassIndex(test.numAttributes() - 1);


        //实验结果的根目录
        String experimentBaseDirectory = MyTools.getBaseInfo("experimentBaseDirectory");
        String enableDataset = MyTools.getBaseInfo("enableDataset").trim();
        String experimentName = null;

        if (enableDataset.equals("Relink")) {
            experimentName = MyTools.getBaseInfo("experimentNameRelink");
        } else if (enableDataset.equals("PROMISE")) {
            experimentName = MyTools.getBaseInfo("experimentNamePROMISE");
        }
        String subDir = experimentName + "/data/NSGAII/";


        Evaluation evaluation = null;

        //遍历每一个集分类
        for (BaseClassiferEnum baseClassiferEnum : BaseClassiferEnum.values()) {

            //数据集和对应的集分类器方法构成的目录
            String dirName = fileName.split("\\.")[0] + baseClassiferEnum;
            //遍历里面的10个文件
            for (int i = 0; i < 10; i++) {
                String tmpFile = experimentBaseDirectory + "-random-" + outerIterate + "/" + subDir + dirName + "/VAR-NSGAII." + i;
                String tmpOut = experimentBaseDirectory + "-random-" + outerIterate + "/" + subDir + dirName + "/FUN-NSGAII-TEST." + i;

                Scanner scanner = null;
                PrintWriter printWriter = null;
                try {
                    scanner = new Scanner(new File(tmpFile));
                    printWriter = new PrintWriter(new File(tmpOut));

                    while (scanner.hasNextLine()) {
                        String line = scanner.nextLine();
                        if(line.trim().equals("")) continue;

                        Instances currentTrain = new Instances(train);
                        Instances currentTest = new Instances(test);

                        int[] toDeletedFeature=MyTools.findToDeletedFeature(line);

                        //断言检验
                        assert toDeletedFeature!=null &&toDeletedFeature.length!=0;

                        evaluation = doValidation(currentTrain, currentTest, baseClassiferEnum, toDeletedFeature);
                        //因为再求解paretofront的时候，我们需要是目标最小化
                        printWriter.println(new Double(train.numAttributes()-toDeletedFeature.length-1) + " " + evaluation.weightedAreaUnderROC());
                    }//while

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    scanner.close();
                    printWriter.close();
                }

            }


        }
    }


    public static void main(String[] args) {
        ArrayList<File> currenctDataSet = null;

        String datasetName = MyTools.getBaseInfo("enableDataset");

        try {
            //遍历每一个数据集中的所有项目
            //String[] strs = datasetName.split(",");
            String[] strs = new String[]{datasetName};
            for (String dataset : strs) {
                System.out.println("开始在测试集上验证：\t" + dataset + "\t" + new Date().toString());


                //得到该数据集中的所有项目
                currenctDataSet = MyTools.getProjects(dataset);

                for (File file : currenctDataSet) {

                    Instances originalInstances = ConverterUtils.DataSource.read(file.toString());

                    //外层10次随机循环
                    for (int outerIterate = 1; outerIterate <= 10; outerIterate++) {
                        System.out.println("正在测试：\t"+file.getName()+"\t 轮次："+outerIterate);

                        //新建一个外层循环的实例集合
                        Instances outerInstances = new Instances(originalInstances);
                        //先进行外层的随机化,为了避免内层随机化与外层的一样，内层的随机因子将在外层的随机因子上加1
                        outerInstances.setClassIndex(outerInstances.numAttributes() - 1);
                        outerInstances.randomize(new Random(outerIterate));
                        if (outerInstances.classAttribute().isNominal()) {
                            outerInstances.stratify(10);
                        }

                        doDefectPrediciton(file.getName(), outerInstances, outerIterate);
                    }
                }

                System.out.println("结束运行项目：\t" + dataset + "\t" + new Date().toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

