package com.bluehouseinc.dataconverter.parsers.orsyp.model;

public class Session {
	Identifier identifier;
	SyntaxCheck syntaxCheck;
	private String functionalVersion;
	private String label;
	Tree tree;
	PostSesList sesList;
	PostDevList postDevList;
	private String _id;

	// Getter Methods

	public Identifier getIdentifier() {
		return identifier;
	}

	public SyntaxCheck getSyntaxCheck() {
		return syntaxCheck;
	}

	public String getFunctionalVersion() {
		return functionalVersion;
	}

	public String getLabel() {
		return label;
	}

	public Tree getTree() {
		return tree;
	}

	public PostSesList getPostSesList() {
		return sesList;
	}

	public PostDevList getPostDevList() {
		return postDevList;
	}

	public String get_id() {
		return _id;
	}

	// Setter Methods

	public void setIdentifier(Identifier identifierObject) {
		this.identifier = identifierObject;
	}

	public void setSyntaxCheck(SyntaxCheck syntaxCheckObject) {
		this.syntaxCheck = syntaxCheckObject;
	}

	public void setFunctionalVersion(String functionalVersion) {
		this.functionalVersion = functionalVersion;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setTree(Tree treeObject) {
		this.tree = treeObject;
	}

	public void setPostSesList(PostSesList postSesListObject) {
		this.sesList = postSesListObject;
	}

	public void setPostDevList(PostDevList postDevListObject) {
		this.postDevList = postDevListObject;
	}

	public void set_id(String _id) {
		this._id = _id;
	}
}
