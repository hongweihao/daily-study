package mkii.hadoop.mapreduce.join.in.reduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * 根据提供的商品表t_product和订单表t_order，在reduce端实现以下的语句：
 * select a.id,a.date,b.name,b.category_id,b.price from t_order a join t_product b on a.pid = b.id
 *
 * 思路：
 * 先在map根据on条件分组，再交给reduce
 *
 * 注意：
 * bean类的序列化与反序列化问题
 *
 */
public class JoinRunner {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://localhost:9000");

        Job job = Job.getInstance(conf);
        job.setJarByClass(JoinRunner.class);

        job.setMapperClass(JoinMapper.class);
        job.setReducerClass(JoinReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(ProductBean.class);

        FileInputFormat.setInputPaths(job, new Path("/join/reduce/input/"));
        FileOutputFormat.setOutputPath(job, new Path("/join/reduce/output"));

        job.waitForCompletion(true);
    }
}
