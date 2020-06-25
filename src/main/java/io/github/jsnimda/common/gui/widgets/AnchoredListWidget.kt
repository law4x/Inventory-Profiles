package io.github.jsnimda.common.gui.widgets

import io.github.jsnimda.common.gui.asPoints
import io.github.jsnimda.common.gui.widget.AnchorStyles
import io.github.jsnimda.common.gui.widget.FlowLayout
import io.github.jsnimda.common.gui.widget.FlowLayout.FlowDirection.TOP_DOWN
import io.github.jsnimda.common.gui.widget.Overflow.VISIBLE
import io.github.jsnimda.common.vanilla.alias.I18n
import io.github.jsnimda.common.vanilla.render.*

private const val COLOR_ANCHOR_BORDER = -0x7f666667
private const val COLOR_ANCHOR_BG = 0x40999999
private const val COLOR_ANCHOR_BORDER_HOVER = -0x666667
private const val COLOR_ANCHOR_BG_HOVER = -0x3f666667
private const val COLOR_WHITE = -0x1
private const val COLOR_BORDER = -0x666667

private const val rowHeight = 13
private const val leastY = 7
private const val SEPARATOR_WIDTH = 10

open class AnchoredListWidget : Widget() {
  var anchorHeader = AnchorHeader()
  val container = ScrollableContainerWidget().apply {
    anchor = AnchorStyles.all
    this@AnchoredListWidget.addChild(this)
    top = anchorHeader.height
    right = 0
    bottom = 0
  }
  val containerContentFlowLayout =
    FlowLayout(container.contentContainer, TOP_DOWN)

  var renderBorder = true
  var borderColor = COLOR_BORDER

  init {
    overflow = VISIBLE
    container.renderBorder = false
    renderBorder = false
  }

  // ============
  // AnchorHeader
  // ============

  inner class AnchorHeader : Widget() {
    private val ellipsisText = I18n.translate("inventoryprofiles.common.gui.config.more")
    private val ellipsisTextWidth = rMeasureText(ellipsisText)

    init {
      anchor = AnchorStyles.noBottom
      this@AnchoredListWidget.addChild(this)
      height = rowHeight
      right = 0
      zIndex = 1
      sizeChanged += {
        // re calc
        if (width != anchorsManager.width) {
          clearChildren()
          anchorsManager.let { old ->
            anchorsManager = AnchorsManager().also { new ->
              // update anchors rowIndexes
              old.anchors.forEach {
                new.addAnchor(it.anchorDisplayText, it.toScrollY)
              }
            }
          }
          // updateTexts()
        }
      }
    }

    fun addAnchor(displayText: String, toScrollY: Int = container.contentHeight) {
      anchorsManager.addAnchor(displayText, toScrollY)
    }

    private val dummyAnchor = Anchor("", 0, 0)

    inner class Anchor(var anchorDisplayText: String, val toScrollY: Int, val rowIndex: Int) {
      val textButtonWidget = TextButtonWidget(anchorDisplayText, { -> container.scrollY = toScrollY }).apply {
        hoverText = "§e§n$anchorDisplayText"
        inactiveText = "§e§n§l$anchorDisplayText"
      }
    }

    var anchorsManager = AnchorsManager()
      private set

    inner class AnchorsManager { // this class manage anchor adding
      val width = this@AnchorHeader.width
      private val availableWidth
        get() = width - 20 - ellipsisTextWidth

      val anchors = mutableListOf<Anchor>()

      private var textLeft = 0
      private var textRowIndex = 0

      val textYPerRow
        get() = (rowHeight + 1) / 2 - 4
      val totalTextRow
        get() = textRowIndex + 1

      fun addAnchor(displayText: String, toScrollY: Int) {
        val textWidth = rMeasureText("§n$displayText")
        if (textLeft + textWidth > availableWidth) {
          textLeft = 0
          textRowIndex++
        }
        textLeft += textWidth + SEPARATOR_WIDTH
        Anchor(displayText, toScrollY, textRowIndex).apply {
          anchors.add(this)
          this@AnchorHeader.addChild(this.textButtonWidget)
        }
        updateTexts()
      }

      fun updateTexts() {
        val startLeft = 10
        var textLeft = startLeft
        var textTop = textYPerRow
        var lastRowIndex = 0
        highlightingAnchor.let { highlightingAnchor ->
          anchors.forEachIndexed { index, anchor ->
            anchor.textButtonWidget.apply {
              active = highlightingAnchor != anchor
              updateWidth()
              visible = expanded || highlightingAnchor.rowIndex == anchor.rowIndex
              if (visible) {
                if (lastRowIndex != anchor.rowIndex) {
                  lastRowIndex = anchor.rowIndex
                  textLeft = startLeft
                  textTop += if (expanded) rowHeight else 0
                }
                top = textTop
                left = textLeft
                textLeft += width + SEPARATOR_WIDTH
              }
            }
          }
        }
      }

      val highlightingAnchorIndex: Int // highlight base on scrollY
        get() {
          val scrollY = container.scrollY
          anchors.forEachIndexed { index, anchor ->
            if (anchor.toScrollY >= scrollY) {
              return if (anchor.toScrollY - scrollY <= container.viewport.height / 2) {
                index
              } else {
                (index - 1).coerceAtLeast(0)
              }
            }
          }
          return anchors.size - 1
        }
      val highlightingAnchor
        get() = if (anchors.isEmpty()) dummyAnchor else anchors[highlightingAnchorIndex]
      val highlightingRowLastAnchor: Anchor
        get() = highlightingAnchorIndex.let { startIndex ->
          if (anchors.isEmpty()) return dummyAnchor
          for (i in startIndex + 1 until anchors.size) {
            if (anchors[i].rowIndex != anchors[startIndex].rowIndex) return anchors[i - 1]
          }
          return anchors.last()
        }
    }

    private var _expanded = false
    var expanded
      get() = _expanded
      set(value) {
        if (value) expand()
        else collaspe()
      }

    fun expand() {
      if (_expanded) return
      _expanded = true
      val leastTop = leastY - this@AnchoredListWidget.screenY
      this.top = (-rowHeight * anchorsManager.highlightingAnchor.rowIndex).coerceAtLeast(leastTop)
      this.height = rowHeight * anchorsManager.totalTextRow + 1
      anchorsManager.updateTexts()
    }

    fun collaspe() {
      if (!_expanded) return
      _expanded = false
      this.top = 0
      this.height = rowHeight
      anchorsManager.updateTexts()
    }

    private var lastHighlightingAnchorIndex = 0
      set(value) {
        if (field != value) {
          field = value
          anchorsManager.updateTexts()
        }
      }

    override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
      expanded = contains(mouseX, mouseY) && anchorsManager.totalTextRow > 1
      val (pt1, pt2) = absoluteBounds.asPoints()
      val (x1, y1) = pt1
      val (x2, y2) = pt2
      rDrawHorizontalLine(x1, x2 - 1, y1, if (expanded) COLOR_ANCHOR_BORDER_HOVER else COLOR_ANCHOR_BORDER)
      rDrawHorizontalLine(x1, x2 - 1, y2, if (expanded) COLOR_ANCHOR_BORDER_HOVER else COLOR_ANCHOR_BORDER)
      rFillRect(x1, y1 + 1, x2, y2, if (expanded) COLOR_ANCHOR_BG_HOVER else COLOR_ANCHOR_BG)
      super.render(mouseX, mouseY, partialTicks)
      lastHighlightingAnchorIndex = anchorsManager.highlightingAnchorIndex
      if (!expanded && anchorsManager.totalTextRow > 1) {
        anchorsManager.highlightingRowLastAnchor.textButtonWidget.run {
          rDrawText(ellipsisText, absoluteBounds.right, screenY, COLOR_WHITE)
        }
      }
    }

    override fun mouseClicked(x: Int, y: Int, button: Int): Boolean =
      true.also { super.mouseClicked(x, y, button) }

  }

  override fun render(mouseX: Int, mouseY: Int, partialTicks: Float) {
    if (renderBorder) {
      rDrawOutline(absoluteBounds, borderColor)
    }
    super.render(mouseX, mouseY, partialTicks)
  }

  fun addEntry(entry: Widget) {
    containerContentFlowLayout.add(entry, entry.height)
    container.contentHeight = containerContentFlowLayout.offset
  }

  fun addAnchor(displayText: String) {
    anchorHeader.addAnchor(displayText)
  }

  fun isOutOfContainer(entry: Widget): Boolean =
    container.scrollY > entry.bounds.bottom || container.scrollY + container.viewport.height < entry.top

}