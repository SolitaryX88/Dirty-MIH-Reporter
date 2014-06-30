// Many thanks to mkyong
// Source http://www.mkyong.com/java/how-to-execute-shell-command-from-java/


import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ExecShell {

	public static String executeCommand(String command) {

		StringBuffer output = new StringBuffer();

		Process p;
		try {
			p = Runtime.getRuntime().exec(command);
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader( p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return output.toString();

	}

}