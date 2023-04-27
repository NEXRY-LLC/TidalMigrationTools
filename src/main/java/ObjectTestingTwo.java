
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import lombok.Data;

public class ObjectTestingTwo {

	public static void main(String[] args) {
		ObjectTestingTwo ot = new ObjectTestingTwo();

		FinalDataModel fd = ot.new FinalDataModel();

		FinalObject p = ot.new FinalObject();

	}

	public class FinalDataModel extends BaseDataModel<FinalObject> {

	}

	public class FinalObject extends BaseObject {

	}

	@Data
	public abstract class BaseObject {

		public int id;
		public String name;
		public List<BaseObject> children;

		public BaseObject() {
			this.children = new LinkedList<>();
		}

		@SuppressWarnings("unchecked")
		public <E extends BaseObject> List<E> getChildren() {
			return (List<E>) this.children;
		}

	}

	@Data
	public abstract class BaseDataModel<E extends BaseObject> {

		List<E> data;

		public BaseDataModel() {
			this.data = new LinkedList<>();
		}

		public void doHandle() {
			toFlatStream(this.data);
		}

		protected Stream<E> toFlatStream(Collection<E> collection) {
			Stream<E> result = collection.stream().flatMap(parent -> {
				if ((parent.getChildren().size() > 0)) {
					return Stream.concat(Stream.of(parent), toFlatStream(parent.getChildren()));
				} else {
					return Stream.of(parent);
				}
			});

			return result;
		}
	}

}
