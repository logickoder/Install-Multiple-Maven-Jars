package jeffreyorazulike.installmavenjars;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * InstallMavenJars Installs maven jars
 */
public class InstallMavenJars {
    // the path the jars should be kept
    private static final String MAVEN_REPOS = "\\jeffreyorazulike\\installmavenjars\\maven repos";
    // the full path the jars should be kept
    private static final File JAR_PATH = new File(System.getProperty("user.dir") + MAVEN_REPOS);

    /**
     * 
     * @return an array of the folders in the {@code MAVEN_REPOS} folder which act
     *         as the group id for a group of jars
     */
    public File[] getGroupIds() {
        if (!JAR_PATH.exists()) // cancel the method if the maven repos path doesn't exist
            return null;

        // creates a filename filter that returns only true for directories
        FilenameFilter folderFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return new File(dir + "\\" + name).isDirectory();
            }
        };

        // gets a list of directories in the MAVEN_REPOS folder and saves them as a file
        // object in the array
        File[] groupIds = JAR_PATH.listFiles(folderFilter);

        return groupIds;
    }

    /**
     * @param groupId the directory housing the jar files
     * @return a list of the jar files in the {@link GroupId} folder
     */
    public File[] getJars(File groupId) {
        // creates a filename filter that returns only files that ends with .jar
        FilenameFilter jarFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar");
            }
        };

        // save all the files that end with .jar in the array
        File[] jars = groupId.listFiles(jarFilter);

        return jars;
    }

    /**
     * 
     * 
     * @param fileName the name of the maven jar file to get the artifact id and
     *                 version
     * 
     * @return the artifact id and the version seperated by \
     */
    public static String getArtifactIdAndVersion(File file) {
        // remove the .jar from the filename
        String fileName = file.getName().substring(0, file.getName().length() - 4);

        StringBuilder artifactIdAndVersion = new StringBuilder(fileName.length());
        String[] arr = fileName.split("-"); // split the file name by -

        for (String string : arr) {
            // if the first character is not a digit and the string does not contain a full
            // stop it is part of the artifact id, if not ut us the version
            if (!(Character.isDigit(string.charAt(0)) && string.contains(".")))
                artifactIdAndVersion.append(string + "-"); // append the artifact id
            else {
                // remove "-" from the end of the string and append the version to the string
                // sperated by /
                artifactIdAndVersion.deleteCharAt(artifactIdAndVersion.length() - 1).append("/" + string);
                break;
            }
        }
        return artifactIdAndVersion.toString();
    }

    private MavenJar[] getStructure() {
        ArrayList<MavenJar> files = new ArrayList<MavenJar>(800);

        File[] groupIds = getGroupIds(); // holds the group ids

        if (groupIds == null) {
            return null; // cancels the method if no groupid is returned
        }

        for (File groupId : groupIds) {
            File[] jarFiles = getJars(groupId);
            for (File jarFile : jarFiles) {
                files.add(new MavenJar(groupId, jarFile));
            }
        }

        return files.toArray(new MavenJar[files.size()]);
    }

    /**
     * <p>
     * Installs the jars to your maven local repository
     * </p>
     */
    public void executeCommand() {
        System.out.println("Getting all the files...");
        MavenJar[] files = getStructure();
        System.out.println("Gotten all the files");

        // i am using a process builder to run the command
        ProcessBuilder builder = new ProcessBuilder();
        String[] commands = { "cmd.exe", "/c", "" };

        MavenJar file = null; // holds the file being handled
        for (int i = 0; i < files.length; ++i) {
            file = files[i];

            // saves the command to execute in cmd, "cd" changes the execution path of the
            // command line to the path of the group id, "&&" means i want to run another
            // command and the rest of the string is the command for installing a maven jar
            // file to the local repo
            commands[2] = String.format("cd " + file.groupId.getAbsolutePath().toString()
                    + " && mvn install:install-file -DgroupId=%s -DartifactId=%s -Dversion=%s -Dfile=%s -Dpackaging=%s -DgeneratePom=true",
                    file.groupId.getName(), file.artifactId, file.version, file.jar.getName(), file.extension);

            // Courtsey, Luke Woodward from stackoverflow
            try {

                // sets the process builer command
                builder.command(commands);

                // this allows me to redirect the process's standard error into its standard
                // output, by calling redirectErrorStream(true). Doing so gives me only one
                // stream to read from, this means that i want redirect anything written on the
                // command line to my program

                builder.redirectErrorStream(true);
                // starts the process
                Process process = builder.start();
                // creates a new buffered reader to read the text from the command line
                BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                // reads the text from the command line and prints it in the program
                while (true) {
                    line = input.readLine();
                    if (line == null) {
                        break;
                    }
                    System.out.println(line);
                }

            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    /**
     * <p>
     * The {@code MavenJar} class holds the details of a maven repo file or whatever
     * like the extension, artifact id, version and whatever
     * </p>
     */
    class MavenJar {
        String artifactId, version, extension; // the jar file artifact id, version and extension
        File groupId, jar; // the jar file and group id

        public MavenJar(File groupId, File jar) {
            this.groupId = groupId;
            this.jar = jar;
            // seperate the artifactid and version
            String[] artifactIdAndVersion = getArtifactIdAndVersion(jar).split("/");
            artifactId = artifactIdAndVersion[0];
            version = artifactIdAndVersion[1];
            extension = jar.getName().substring(jar.getName().length() - 3);
        }

    }

    public static void main(String[] args) {
        InstallMavenJars installMavenJars = new InstallMavenJars();
        installMavenJars.executeCommand();
        System.out.println("Done installing the files, please give me a star on github, love you");
    }
}