package com.wurmonline.womconverter;

import java.io.File;
import javafx.application.Application;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.wurmonline.womconverter.converters.AssimpToWOMConverter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Main extends Application {
    
    public static void main(String[] args) {
        if (args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("-h"))) {
            System.out.println("Usage:");
            System.out.println("java -jar WOM_Converter.jar [-generatetangents] [-recursive] [-indir input_directory] [-outdir output_directory] input_files_regex");
            System.out.println("Options:");
            System.out.println("-generatetangents : automatically generate tangent and binormal values if they aren't present in input files. Default: off.");
            System.out.println("-recursive : export files recursively in all subfolders relative to input directory, will create output directory folders accordingly. Default: off.");
            System.out.println("-indir input_directory : look for input files in input_directory. Must be a directory. Default: current dir.");
            System.out.println("-outdir output_directory : output directory for output files. Must be a directory. Default: current dir.");
            System.out.println("input_files_regex : regex used to lookup the input files to convert.");
            System.out.println("Examples:");
            System.out.println("java -jar WOM_Converter.jar -generatetangents .+dae");
            System.out.println("Will take all dae files in current directory, convert them to WOM generating tangent and binormal values when needed and export to current directory");
            System.out.println("java -jar WOM_Converter.jar -devfilechooser");
            System.out.println("Will skip normal program execution and ignore other options, opening file manager to quickly test exporting of single model");

            launch(args);
        }
        
        boolean generateTangents = false;
        boolean recursive = false;
        String inputDirectory = "";
        String outputDirectory = "";
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            
            switch (arg) {
                case "-devfilechooser":
                    launch(args);
                    break;
                case "-generatetangents":
                    generateTangents = true;
                    break;
                case "-recursive":
                    recursive = true;
                    break;
                case "-indir":
                    i++;
                    inputDirectory = args[i];
                    break;
                case "-outdir":
                    i++;
                    outputDirectory = args[i];
                    break;
            }
        }
        
        String inputRegex = args[args.length - 1];
        
        File inputDirectoryFile = new File(inputDirectory);
        if (!inputDirectoryFile.isDirectory()) {
            System.err.println("Input directory is not a valid directory: " + inputDirectory);
            return;
        }
        File outputDirectoryFile = new File(outputDirectory);
        if (!outputDirectoryFile.isDirectory()) {
            System.err.println("Output directory is not a valid directory: " + outputDirectory);
            return;
        }
        
        try {
            convertFiles(inputDirectoryFile, outputDirectoryFile, inputRegex, recursive, generateTangents);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.exit(0);
    }
    
    private static void convertFiles(File inputDirectory, File outputDirectory, String inputRegex, boolean recursive, boolean generateTangents) throws IOException {
        File[] filteredFiles = inputDirectory.listFiles((File file) -> {
            if (file.isDirectory()) {
                return false;
            }

            return file.getName().matches(inputRegex);
        });
        
        for (File file : filteredFiles) {
            AssimpToWOMConverter.convert(file, outputDirectory, generateTangents);
        }
        
        if (recursive) {
            File[] directories = inputDirectory.listFiles((File file) -> {
                return file.isDirectory();
            });
            
            for (File directory : directories) {
                String name = directory.getName();
                
                File newInputDirectory = directory;
                File newOutputDirectory = new File(outputDirectory, name);
                convertFiles(newInputDirectory, newOutputDirectory, inputRegex, recursive, generateTangents);
            }
        }
    }

    public void start(Stage primaryStage) throws Exception {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Collada Model");
        
        File modelFile = fileChooser.showOpenDialog(primaryStage);
        
        if (modelFile == null) {
            System.exit(0);
        }
        
        AssimpToWOMConverter.convert(modelFile, modelFile.getParentFile(), true);
        
        System.exit(0);
    }
    
}
