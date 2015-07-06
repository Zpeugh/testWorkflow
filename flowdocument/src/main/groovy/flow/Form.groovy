package flow

/**
 * Created by zpeugh on 5/22/15.
 */
class Form {

	int formID
	String formName
	String formClass
	def fieldTemplates

	public String toString(){
		return "${this.formName}"
	}

}
