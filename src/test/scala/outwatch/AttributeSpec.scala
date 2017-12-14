package outwatch

import outwatch.dom._
import outwatch.dom.dsl._

class AttributeSpec extends JSDomSpec {

  "data attribute" should "correctly render only Data" in {
    val node = input(
      data.geul := "bar",
      data.geuli.gurk := "barz"
    ).map(_.asProxy).unsafeRunSync()

    node.data.attrs.toList should contain theSameElementsAs List(
      "data-geul" -> "bar",
      "data-geuli-gurk" -> "barz"
    )
  }

  it should "correctly render only expanded data with dynamic content" in {
    val node = input(
      dataAttr("geul") := "bar",
      dataAttr("geuli-gurk") := "barz"
    ).map(_.asProxy).unsafeRunSync()

    node.data.attrs.toList should contain theSameElementsAs List(
      "data-geul" -> "bar",
      "data-geuli-gurk" -> "barz"
    )
  }

  it should "not compile data.without suffix" in {
    """input(data.:= "bar")""" shouldNot compile
  }

  //TODO: doesn't compile but test still fails
//   it should "not compile data without suffix" in {
//     """input(data := "bar")""" shouldNot compile
//   }

  "attr/prop/style" should "correctly render type" in {
    val node = tag("input")(
      attr("foo") := "foo",
      attr[Boolean]("boo", identity) := true,
      attr[Boolean]("yoo", x => if (x) "yes" else "no") := true,
      prop("bar") := "bar",
      prop("num") := 12,
      style("baz") := "baz",
      contentEditable := false,
      autoComplete := false,
      disabled := false
    ).map(_.asProxy).unsafeRunSync()

    node.data.attrs.toList should contain theSameElementsAs List(
      "foo" -> "foo",
      "boo" -> true,
      "yoo" -> "yes",
      "contenteditable" -> "false",
      "autocomplete" -> "off",
      "disabled" -> false
    )
    node.data.props.toList should contain theSameElementsAs List(
      "bar" -> "bar",
      "num" -> 12
    )
    node.data.style.toList should contain theSameElementsAs List(
      "baz" -> "baz"
    )
  }

  "optional attributes" should "correctly render" in {
    val node = input(
      data.foo :=? Option("bar"),
      data.bar :=? Option.empty[String]
    ).map(_.asProxy).unsafeRunSync()

    node.data.attrs.toList should contain theSameElementsAs List(
      "data-foo" -> "bar"
    )
  }

  "apply on vtree" should "correctly merge attributes" in {
    val node = input(
      data.a := "bar",
      data.a.gurke := "franz"
    )(
      data.a := "buh",
      data.a.tomate := "gisela"
    ).map(_.asProxy).unsafeRunSync()

    node.data.attrs.toList should contain theSameElementsAs List(
      "data-a" -> "buh",
      "data-a-gurke" -> "franz",
      "data-a-tomate" -> "gisela"
    )
  }

  it should "correctly merge styles written with style" in {
    val node = input(
      style("color") := "red",
      fontSize:= "5px"
    )(
      style("color") := "blue",
      border := "1px solid black"
    ).map(_.asProxy).unsafeRunSync()

    node.data.style.toList should contain theSameElementsAs List(
      ("color", "blue"),
      ("font-size", "5px"),
      ("border", "1px solid black")
    )
  }

  it should "correctly merge styles" in {
    val node = input(
      color.red,
      fontSize:= "5px"
    )(
      color.blue,
      border := "1px solid black"
    ).map(_.asProxy).unsafeRunSync()

    node.data.style.toList should contain theSameElementsAs List(
      ("color", "blue"),
      ("font-size", "5px"),
      ("border", "1px solid black")
    )
  }

  it should "correctly merge keys" in {

    val node = input( attributes.key := "bumm")( attributes.key := "klapp").map(_.asProxy).unsafeRunSync()
    node.data.key.toList should contain theSameElementsAs List("klapp")

    val node2 = input()( attributes.key := "klapp").map(_.asProxy).unsafeRunSync()
    node2.data.key.toList should contain theSameElementsAs List("klapp")

    val node3 = input( attributes.key := "bumm")().map(_.asProxy).unsafeRunSync()
    node3.data.key.toList should contain theSameElementsAs List("bumm")
  }

  "style attribute" should "render correctly" in {
    val node = input(color.red).map(_.asProxy).unsafeRunSync()

    node.data.style.toList should contain theSameElementsAs List(
      "color" -> "red"
    )
  }
}
