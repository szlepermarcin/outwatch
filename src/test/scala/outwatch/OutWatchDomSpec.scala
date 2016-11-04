package outwatch

import org.scalajs.dom.raw.HTMLInputElement
import org.scalatest.BeforeAndAfterEach
import rxscalajs.{Observable, Subject}
import snabbdom.{DataObject, h}
import outwatch.dom._
import outwatch.dom.helpers._

import scala.collection.immutable.Seq
import scala.scalajs.js
import scala.scalajs.js.JSON
import org.scalajs.dom.document

class OutWatchDomSpec extends UnitSpec with BeforeAndAfterEach {

  override def afterEach(): Unit = {
    document.body.innerHTML = ""
  }

  "VDomModifiers" should "be seperated correctly" in {
    val modifiers = Seq(
      Attribute("class", "red"),
      EventEmitter("click", Subject()),
      VDomModifier.StringNode("Test"),
      DomUtils.constructVNode("div", Seq(), Seq(), None, Seq(), Seq(), Seq()),
      AttributeStreamReceiver("hidden",Observable.of())
    )

    val (emitters, child$, children$, attributes, attribute$, children) = DomUtils.seperateModifiers(modifiers: _*)

    emitters.length shouldBe 1
    child$.length shouldBe 0
    attributes.length shouldBe 1
    attribute$.length shouldBe 1
    children.length shouldBe 2


  }


  it should "be seperated correctly with children" in {
    val modifiers = Seq(
      Attribute("class","red"),
      EventEmitter("click",Subject()),
      InputEventEmitter("input", Subject()),
      AttributeStreamReceiver("hidden",Observable.of()),
      AttributeStreamReceiver("disabled",Observable.of()),
      ChildrenStreamReceiver(Observable.of()),
      KeyEventEmitter("keyup", Subject())
    )

    val (emitters, child$, children$, attributes,attribute$, children) = DomUtils.seperateModifiers(modifiers: _*)

    emitters.length shouldBe 3
    child$.length shouldBe 0
    children$.length shouldBe 1
    attributes.length shouldBe 1
    attribute$.length shouldBe 2
    children.length shouldBe 0

  }

  val fixture = new {
    val proxy = h("div", DataObject(js.Dictionary("class" -> "red", "id" -> "msg"), js.Dictionary()), js.Array(
      h("span", DataObject(js.Dictionary(), js.Dictionary()), js.Array("Hello"))
    ))
  }

  "VTrees" should "be constructed correctly" in {

    val attributes = List(Attribute("class", "red"), Attribute("id", "msg"))
    val message = "Hello"
    val children = List(DomUtils.constructVNode("span", Seq(), Seq(), None, Seq(), Seq(), Seq(message)))
    val nodeType = "div"
    val vtree = DomUtils.constructVNode(nodeType, Seq(),Seq(),None, attributes, Seq(), children).asInstanceOf[VDomModifier.VTree]

    vtree.nodeType shouldBe nodeType
    vtree.children.length shouldBe 1

    val proxy = fixture.proxy

    JSON.stringify(vtree.asProxy) shouldBe JSON.stringify(proxy)

  }

  it should "be correctly created with the HyperscriptHelper" in {
    val attributes = List(Attribute("class", "red"), Attribute("id", "msg"))
    val message = "Hello"
    val child = DomUtils.hyperscriptHelper("span")(message)
    val nodeType = "div"
    val vtree = DomUtils.hyperscriptHelper(nodeType)(attributes.head, attributes(1), child)

    JSON.stringify(vtree.asProxy) shouldBe JSON.stringify(fixture.proxy)
  }


  it should "be correctly patched into the DOM" in {
    val id = "msg"
    val cls = "red"
    val attributes = List(Attribute("class", cls), Attribute("id", id))
    val message = "Hello"
    val child = DomUtils.hyperscriptHelper("span")(message)
    val nodeType = "div"
    val vtree = DomUtils.hyperscriptHelper(nodeType)(attributes.head, attributes(1), child)


    val node = document.createElement("div")
    document.body.appendChild(node)

    DomUtils.render(node, vtree)

    val patchedNode = document.getElementById(id)

    patchedNode.childElementCount shouldBe 1
    patchedNode.classList.contains(cls) shouldBe true
    patchedNode.children(0).innerHTML shouldBe message

  }

  "The HTML DSL" should "construct VTrees properly" in {
    import outwatch.dom._

    val vtree = div(cls := "red", id := "msg",
      span("Hello")
    )

    JSON.stringify(vtree.asProxy) shouldBe JSON.stringify(fixture.proxy)

  }

  it should "patch into the DOM properly" in {
    import outwatch.dom._

    val message = "Test"
    val vtree = div(cls := "blue", id := "test",
      span(message),
      ul(id := "list",
        li("1"),
        li("2"),
        li("3")
      )
    )

    val node = document.createElement("div")
    document.body.appendChild(node)

    DomUtils.render(node, vtree)

    val patchedNode = document.getElementById("test")

    patchedNode.childElementCount shouldBe 2
    patchedNode.classList.contains("blue") shouldBe true
    patchedNode.children(0).innerHTML shouldBe message

    document.getElementById("list").childElementCount shouldBe 3

  }

  it should "change the value of a textfield" in {
    import outwatch.dom._

    val messages = Subject[String]
    val vtree = div(
      input(outwatch.dom.value <-- messages, id := "input")
    )

    val node = document.createElement("div")
    document.body.appendChild(node)

    DomUtils.render(node, vtree)

    val field = document.getElementById("input").asInstanceOf[HTMLInputElement]

    field.value shouldBe ""

    val message = "Hello"
    messages.next(message)

    field.value shouldBe message

    val message2 = "World"
    messages.next(message2)

    field.value shouldBe message2

  }

}
