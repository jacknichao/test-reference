package jmetal.nichao.testreferences;

import jmetal.qualityIndicator.util.MetricsUtil;
import jmetal.util.NonDominatedSolutionList;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GenerateTestParetoFront {

    private static String experimentBaseDirectory = null;
    private static String enableDataset = null;
    private static String experimentName = null;
    private static String subDir = null;

    static {
        //实验结果的根目录
        experimentBaseDirectory = MyTools.getBaseInfo("experimentBaseDirectory");
        enableDataset = MyTools.getBaseInfo("enableDataset").trim();
        experimentName = null;

        if (enableDataset.equals("Relink")) {
            experimentName = MyTools.getBaseInfo("experimentNameRelink");
        } else if (enableDataset.equals("PROMISE")) {
            experimentName = MyTools.getBaseInfo("experimentNamePROMISE");
        }

        subDir = experimentName + "/data/NSGAII/";

    }


    /**
     * 生成引用前沿
     *
     * @param problemFulPath 目标问题的目录
     * @param outParentDir   输出的引用前沿跟目录
     */
    public static void generateReferenceFronts(String problemFulPath, String outParentDir) {

        MetricsUtil metricsUtils = new MetricsUtil();
        NonDominatedSolutionList solutionSet = new NonDominatedSolutionList();

        for (int numRun = 0; numRun < 10; numRun++) {
            String outputParetoFrontFilePath = problemFulPath + "/FUN-NSGAII-TEST." + numRun;
            metricsUtils.readNonDominatedSolutionSet(outputParetoFrontFilePath, solutionSet);
        } // for

        solutionSet.printObjectivesToFile(outParentDir + "/" + new File(problemFulPath).getName() + ".rf");
    } // generateReferenceFronts


    /**
     *生成NSGAII的引用的参照前沿
     */
    public static void generateReferenceNSGAIIFronts() {
        System.out.println("Starting generate NSGAII referencesFront");

        //10个轮次的独立运行结果
        for (int i = 1; i <= 10; i++) {

            String parent = experimentBaseDirectory + "-random-" + i + "/" + subDir;
            String outParentDir = experimentBaseDirectory + "-random-" + i +"/"+experimentName +"/referenceFrontsNSGAII";
            System.out.println("Generating:\t"+experimentName+"\tindenpendentIndex:\t"+i);

            File rfDirectory = new File(outParentDir);
            if (!rfDirectory.exists()) {
                boolean result = new File(outParentDir).mkdirs();
                System.out.println("Creating " + outParentDir);
            }

            //遍历每一个问题,即每一个目录
            for (File problemDir : new File(parent).listFiles()) {
                //进入每一个问题的文件夹
                if (problemDir.isDirectory()) {
                    generateReferenceFronts(problemDir.getPath(), outParentDir);
                }
            }

        }
    } // generateReferenceFronts

    public static void main(String[] args) {

        generateReferenceNSGAIIFronts();
    }

}
