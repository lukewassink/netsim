package visualizer.test_util

import com.raquo.domtestutils.matching.RuleImplicits
import org.scalactic.Tolerance
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import com.raquo.domtestutils.scalatest.MountSpec
import com.raquo.domtestutils.matching.*
import com.raquo.laminar.keys.*
import com.raquo.laminar.nodes.*
import com.raquo.laminar.tags.Tag

class UnitSpec
    extends AnyFunSpec
    with Matchers
    with MountSpec
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

  override implicit def makeTagTestable(tag: Tag.Base): ExpectedNode = {
    ExpectedNode.element(tag.name)
  }

  override implicit def makeCommentBuilderTestable(
      commentBuilder: () => CommentNode
  ): ExpectedNode = {
    ExpectedNode.comment
  }

  override implicit def makeHtmlPropTestable[V, _DomV](prop: HtmlProp[V] {
    type DomV = _DomV
  }): TestableHtmlProp[V, _DomV] = {
    new TestableHtmlProp[V, _DomV](prop.name, prop.codec.decode)
  }

  override implicit def makeStyleTestable[V](
      style: StyleProp[V]
  ): TestableStyleProp[V] = {
    new TestableStyleProp[V](style.name)
  }

  override implicit def makeGlobalAttrTestable[V](
      attr: GlobalAttr[V]
  ): TestableGlobalAttr[V] = {
    new TestableGlobalAttr[V](attr.name, attr.codec.encode, attr.codec.decode)
  }

  override implicit def makeHtmlAttrTestable[V](
      attr: HtmlAttr[V]
  ): TestableHtmlAttr[V] = {
    new TestableHtmlAttr[V](attr.name, attr.codec.encode, attr.codec.decode)
  }

  override implicit def makeSvgAttrTestable[V](
      svgAttr: SvgAttr[V]
  ): TestableSvgAttr[V] = {
    new TestableSvgAttr[V](
      svgAttr.name,
      svgAttr.codec.encode,
      svgAttr.codec.decode,
      svgAttr.namespaceUri
    )
  }

  override implicit def makeMathMlAttrTestable[V](
      attr: MathMlAttr[V]
  ): TestableMathMlAttr[V] = {
    new TestableMathMlAttr[V](attr.name, attr.codec.encode, attr.codec.decode)
  }

  override implicit def makeCompositeKeyTestable(
      key: CompositeAttr[?]
  ): TestableCompositeKey = {
    new TestableCompositeKey(
      key.name,
      key.separator,
      getRawDomValue = _.getAttribute(key.name)
    )
  }
}
