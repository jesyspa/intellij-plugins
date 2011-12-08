package cocoa.renderer {
import cocoa.Border;
import cocoa.View;
import cocoa.pane.PaneItem;
import cocoa.text.TextFormat;
 
import flash.display.DisplayObjectContainer;
 
import flash.display.Graphics;
import flash.display.Shape;
import flash.display.Sprite;
import flash.text.engine.TextLine;
 
public class PaneRendererManager extends TextRendererManager {
  private var border:Border;
 
  private static const paneTitleEntryFactory:ViewEntryFactory = new ViewEntryFactory();
 
  public function PaneRendererManager(textFormat:TextFormat, border:Border) {
    super(textFormat, border.contentInsets);
 
    this.border = border;
    registerEntryFactory(paneTitleEntryFactory);
  }
 
  protected var titleCanvasContainer:Sprite;
 
  override public function set container(value:DisplayObjectContainer):void {
    super.container = value;
 
    if (titleCanvasContainer == null) {
      titleCanvasContainer = new Sprite();
      titleCanvasContainer.mouseEnabled = false;
      titleCanvasContainer.mouseChildren = false;
    }
 
    value.addChild(titleCanvasContainer);
  }
 
  override protected function createEntry(itemIndex:int, x:Number, y:Number, w:Number, h:Number):TextLineEntry {
    var line:TextLine = createTextLine(itemIndex, w);
    layoutTextLine(line, x, y, border.layoutHeight);
 
    var e:ViewEntry = ViewEntry(paneTitleEntryFactory.create(line));
    var shape:Shape = Shape(e.displayObject);
    if (shape.parent != titleCanvasContainer) {
      titleCanvasContainer.addChild(shape);
    }
    
    shape.y = y;
    shape.x = x;
    var g:Graphics = shape.graphics;
    g.clear();
    border.draw(g, w, border.layoutHeight);
 
    var item:PaneItem = PaneItem(_dataSource.getObjectValue(itemIndex));
    var view:View = item.view;
    if (view == null) {
      view = item.viewFactory.newInstance();
      item.view = view;
    }
    
    e.view = view;
 
 
    _lastCreatedRendererDimension = border.layoutHeight;
    return e;
  }
}
}
