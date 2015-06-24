package flow
/**
 * Created by zpeugh on 5/22/15.
 */
class Action {
	int actionID
	String actionName
	String actionLabel
	def actionParameters
	int actionVersion

	public String toString(){
		return "${this.actionName}"
	}

}
