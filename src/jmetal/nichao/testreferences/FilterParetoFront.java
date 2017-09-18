package jmetal.nichao.testreferences;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * filter pareto front with zero features
 */
public class FilterParetoFront {

    public static void main(String[] args) {
        System.out.println("Starting to filter:");
        String experimentBaseDirectory=MyTools.getBaseInfo("experimentBaseDirectory");

        String enableDataset=MyTools.getBaseInfo("enableDataset").trim();
        String experimentName=null;

        if(enableDataset.equals("Relink")){
            experimentName=MyTools.getBaseInfo("experimentNameRelink");
        }else if(enableDataset.equals("PROMISE")){
            experimentName=MyTools.getBaseInfo("experimentNamePROMISE");
        }

        int independentRuns= Integer.parseInt(MyTools.getBaseInfo("independentRuns"));
        String rootPath=null;
        String subDir=experimentName+"/data/NSGAII/";

        Scanner funIn=null;
        Scanner varIn=null;

        PrintWriter funOut=null;
        PrintWriter varOut=null;

        for(int randomIndex=1;randomIndex<=independentRuns;randomIndex++){
            rootPath=experimentBaseDirectory+"-random-"+randomIndex+"/";

            String tmp=rootPath+subDir;
            //每一个问题文件
            for(File file: new File(tmp).listFiles() ) {
                String problemPath=file.toString();
                System.out.println("Filtering:\t"+problemPath);

                for(int i=0;i<independentRuns;i++) {

                    try {
                        funIn = new Scanner(new File(problemPath+"/FUN."+i));
                        varIn = new Scanner(new File(problemPath+"/VAR."+i));

                        funOut = new PrintWriter(new File(problemPath+"/FUN-NSGAII."+i));
                        varOut = new PrintWriter(new File(problemPath+"/VAR-NSGAII."+i));

                        //FUN 和 VAR文件的行数是一样的
                        while (funIn.hasNextLine()) {
                            String fun = funIn.nextLine();
                            String var = varIn.nextLine();

                            if(fun.trim().equals("")) continue;


                            //当前行选取了0个特征，这是不允许的因此需要过滤掉
                            if (fun.split(" ")[0].trim().equals("0.0")) {
                                continue;
                            }

                            //保存当前有效的数据：至少选择一个特征，以及对应的性能指标
                            funOut.println(fun);
                            varOut.println(var);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        funIn.close();
                        varIn.close();
                        funOut.close();
                        varOut.close();
                    }
                }//end for indenpendent
            }//end for problem

        }//end for indenpendentRuns

    }

    /**
     * 检测指定的目录是否存在如果不存在则创建（含父目录）
     * @param dir
     */
    private void checkDir(String dir){
        if(! Files.exists(Paths.get(dir))){
            try {
                Files.createDirectories(Paths.get(dir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
