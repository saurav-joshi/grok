package com.iaasimov.workflow;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class S3Handler implements Serializable {

    //public static final String accessKey = "AKIAJKSZZOYOAH7TKBLQ";
    //public static final String secretKey = "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";

    //private static final String accessKey = "AKIAIAHREBFNCBT43EPA ";
    //private static final String secretKey = "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";


//    public static String getAccessKey() {
//        return accessKey;
//    }
//
//    public  static String getSecretKey() {
//        return secretKey;
//    }


    public static List<String> getFilesInFolder(String bucketName, String folderName) {
        //AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials(getAccessKey(), getSecretKey()));

      BasicAWSCredentials credentials = new BasicAWSCredentials(GlobalConstantsNew.getInstance().accessKey, GlobalConstantsNew.getInstance().secretKey);



      String endpoint = "https://" + "apaciaas" + ".compat.objectstorage.us-ashburn-1.oraclecloud.com";
      String region = "us-ashburn-1";
      //String endpoint = "https://" + "s3.amazonaws.com";
      //String region = "us-east-1";

      AmazonS3 s3 =  AmazonS3Client.builder().standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .disableChunkedEncoding().enablePathStyleAccess().build();

        ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
            .withBucketName(bucketName)
            .withPrefix(folderName + "/");

      ObjectListing object_listing = s3.listObjects("iaasimov-taxonomy");


        List<Bucket> b = s3.listBuckets();



        List<String> files = new ArrayList<>();

        ObjectListing objectListing;
        do {
            objectListing = s3.listObjects(listObjectsRequest);
            files.addAll(
                objectListing.getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey).collect(Collectors.toList())
            );
            listObjectsRequest.setMarker(objectListing.getNextMarker());
        } while (objectListing.isTruncated());

        System.out.println("Read from S3  ==>> "+ files);
        return files;
    }

    public static List<String> readLinesFromFile(String bucketName, String key) throws IOException {
        //AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials(getAccessKey(), getSecretKey()));
        //S3Object s3object = s3.getObject(new GetObjectRequest(bucketName, key));

      String endpoint = "https://" + "apaciaas" + ".compat.objectstorage.us-ashburn-1.oraclecloud.com";
      String region = "us-ashburn-1";
      BasicAWSCredentials credentials = new BasicAWSCredentials(GlobalConstantsNew.getInstance().accessKey, GlobalConstantsNew.getInstance().secretKey);
      AmazonS3 s3 =  AmazonS3Client.builder().standard()
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .disableChunkedEncoding().enablePathStyleAccess().build();

      S3Object s3object = s3.getObject(new GetObjectRequest(bucketName, key));

        List<String> lines = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(s3object.getObjectContent(), "8859_1"));
        String line;
        while((line = reader.readLine()) != null) {
            //System.out.println(line);
            lines.add(line.trim());
        }

//        System.out.println(lines.get(0));
        return lines;
    }

    public static void writeLinesToFile(String bucketName, String key, List<String> lines) throws IOException {
        AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials(GlobalConstantsNew.getInstance().accessKey, GlobalConstantsNew.getInstance().secretKey));

        byte[] data = lines.stream().collect(Collectors.joining("\n")).getBytes(Charset.defaultCharset());
        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentLength(data.length);
        s3.putObject(new PutObjectRequest(bucketName, key, new ByteArrayInputStream(data), metaData));
    }

}
