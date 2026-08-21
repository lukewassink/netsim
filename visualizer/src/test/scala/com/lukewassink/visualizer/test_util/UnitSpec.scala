package com.lukewassink.visualizer.test_util

import com.raquo.domtestutils.matching.RuleImplicits
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import com.raquo.domtestutils.scalatest.MountSpec
import com.raquo.domtestutils.matching.*
import com.raquo.laminar.keys.*
import com.raquo.laminar.nodes.*
import com.raquo.laminar.tags.Tag
import com.lukewassink.simulation.test_utils.ImplicitConversions
import com.raquo.airstream.ownership.{Owner, Subscription}
import org.scalactic
import com.raquo.laminar.api.L
import org.scalatest.BeforeAndAfter

// Don't generate compiler warnings for the implicit conversions below. They are required for domtestutils.
import scala.language.implicitConversions

class TestableOwner extends Owner {

  def _testSubscriptions: List[Subscription] = subscriptions.asScalaJs.toList

  override def killSubscriptions(): Unit = super.killSubscriptions()
}

class UnitSpec
    extends AnyFunSpec
    with Matchers
    with MountSpec
    with BeforeAndAfter
    with RuleImplicits[
      Tag.Base,
      CommentNode,
      HtmlProp,
      GlobalAttr,
      HtmlAttr,
      SvgAttr,
      MathMlAttr,
      StyleProp,
      CompositeAttr[?]
    ] {

  // Allows us to call .observe on Airstream Signals in tests
  // so that we can get their value with .now().
  given owner: TestableOwner = TestableOwner()
  after(owner.killSubscriptions())

  var root: RootNode = null

  def sentinel: ExpectedNode = ExpectedNode.comment

  /** You can use this when `sentinel` does not make sense semantically */
  def emptyCommentNode: ExpectedNode = ExpectedNode.comment

  def mount(using
      prettifier: scalactic.Prettifier,
      pos: scalactic.source.Position
  )(
      node: ReactiveElement.Base,
      clue: String = defaultMountedElementClue
  ): Unit = {
    mountedElementClue = clue
    assertEmptyContainer("laminar.mount")
    root = L.render(containerNode, node)
  }

  def mount(clue: String, node: ReactiveElement.Base)(using
      prettifier: scalactic.Prettifier,
      pos: scalactic.source.Position
  ): Unit = mount(using prettifier, pos)(node, clue)

  override def unmount(clue: String = "unmount")(using
      prettifier: scalactic.Prettifier,
      pos: scalactic.source.Position
  ): Unit = {
    assertRootNodeMounted("unmount:" + clue)
    doAssert(
      root != null,
      s"ASSERT FAILED [unmount:$clue]: Laminar root not found. Did you use Laminar's mount() method in LaminarSpec? Note: unfortunately this could conceal the true error message."
    )
    doAssert(
      root.child.ref == rootNode,
      s"ASSERT FAILED [unmount:$clue]: Laminar root's ref does not match rootNode. What did you do!?"
    )
    doAssert(
      root.unmount(),
      s"ASSERT FAILED [unmount:$clue]: Laminar root failed to unmount"
    )
    root = null
    // containerNode = null
    mountedElementClue = defaultMountedElementClue
  }

  export ImplicitConversions.given

  override implicit def makeTagTestable(tag: Tag.Base): ExpectedNode =
    ExpectedNode.element(tag.name)

  override implicit def makeCommentBuilderTestable(
      commentBuilder: () => CommentNode
  ): ExpectedNode = ExpectedNode.comment

  override implicit def makeHtmlPropTestable[V, _DomV](prop: HtmlProp[V] {
    type DomV = _DomV
  }): TestableHtmlProp[V, _DomV] =
    new TestableHtmlProp[V, _DomV](prop.name, prop.codec.decode)

  override implicit def makeStyleTestable[V](
      style: StyleProp[V]
  ): TestableStyleProp[V] = new TestableStyleProp[V](style.name)

  override implicit def makeGlobalAttrTestable[V](
      attr: GlobalAttr[V]
  ): TestableGlobalAttr[V] =
    new TestableGlobalAttr[V](attr.name, attr.codec.encode, attr.codec.decode)

  override implicit def makeHtmlAttrTestable[V](
      attr: HtmlAttr[V]
  ): TestableHtmlAttr[V] =
    new TestableHtmlAttr[V](attr.name, attr.codec.encode, attr.codec.decode)

  override implicit def makeSvgAttrTestable[V](
      svgAttr: SvgAttr[V]
  ): TestableSvgAttr[V] =
    new TestableSvgAttr[V](
      svgAttr.name,
      svgAttr.codec.encode,
      svgAttr.codec.decode,
      svgAttr.namespaceUri
    )

  override implicit def makeMathMlAttrTestable[V](
      attr: MathMlAttr[V]
  ): TestableMathMlAttr[V] =
    new TestableMathMlAttr[V](attr.name, attr.codec.encode, attr.codec.decode)

  override implicit def makeCompositeKeyTestable(
      key: CompositeAttr[?]
  ): TestableCompositeKey =
    new TestableCompositeKey(
      key.name,
      key.separator,
      getRawDomValue = _.getAttribute(key.name)
    )
}
