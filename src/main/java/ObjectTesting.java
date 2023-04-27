
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ObjectTesting {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		Properties p = new Properties();
		InputStream in = new FileInputStream("./cfg/genex/VariableMapping2.txt");
		p.load(in);

		boolean tf = "File transfer for %%SUB_EDT_NAME completed successfully on %%@$DATE. dfasdf".matches(".*(%%[\\${1}|@{1}|.{0}]\\$DATE\\.{1}).*");

		String data = "File transfer for %%SUB_EDT_NAME completed successfully on %%@$DATE. dfasdf";
		Pattern pattern = Pattern.compile(".*(%%[\\${1}|@{1}|.{0}]\\$DATE\\.{1}).*");
		Matcher matcher = pattern.matcher(data);

		while (matcher.find()) {
			int cnt = matcher.groupCount();
			for (int i = 1; i <= cnt; i++) {
				String replace = matcher.group(i);
				data = data.replace(replace, "<systedate>");
			}

			data.chars();
		}

		// ObjectTesting t = new ObjectTesting();
		// t.readInSerialisable();

		// PrimaryBaseObject parent = t.new PrimaryBaseObject();
		//
		// PrimaryBaseObject child = t.new PrimaryBaseObject();
		//
		// parent.addChild(child);
		//
		//
		// toFlatStream(parent.getChildren()).forEach(System.out::print);
	}

	public class PrimaryBaseObject extends BaseObject {

	}

	public abstract class BaseObject {

		List<? super BaseObject> children = new ArrayList<>();
		BaseObject parent;

		public <E extends BaseObject> void addChild(E job) {
			this.children.add(job);
			job.parent = this;
		}

		@SuppressWarnings("unchecked")
		public <E extends BaseObject> List<E> getChildren() {
			return (List<E>) this.children;
		}
	}

	protected static <E extends BaseObject> Stream<E> toFlatStream(Collection<E> collection) {
		Stream<E> result = collection.stream().flatMap(parent -> {
			if ((parent.getChildren().size() > 0)) {
				return Stream.concat(Stream.of(parent), toFlatStream(parent.getChildren()));
			} else {
				return Stream.of(parent);
			}
		});

		return result;
	}

	public void readInSerialisable() {

		try {
			FileInputStream fileIn = new FileInputStream("cfg/StreamPackage.ser");

			DataInputStream in = new DataInputStream(fileIn);

			// You only wrote one object, so only try to read one object back.**
			Object obj = in.read();

			while (in.available() > 0) {
				String k = in.readLine();
				System.out.print(k + " ");
			}
		} catch (Exception exc) {
			System.out.println("didnt work");
			exc.printStackTrace(); // Very useful for findout out exactly what went wrong.
		}
	}

}
