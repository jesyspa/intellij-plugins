package com.intellij.flex.uiDesigner.ui {
import cocoa.ContentView;
import cocoa.ControlView;
import cocoa.plaf.LookAndFeel;

import com.intellij.flex.uiDesigner.DocumentDisplayManager;

import flash.display.Bitmap;
import flash.display.BitmapData;
import flash.display.DisplayObjectContainer;
import flash.display.Graphics;
import flash.display.Sprite;
import flash.geom.Rectangle;

public class DocumentContainer extends ControlView {
  [Embed(source="/grid.png")]
  private static var gridClass:Class;

  private static const gridBitmapData:BitmapData = Bitmap(new gridClass()).bitmapData;

  private var _documentSysteManager:Sprite;

  public function DocumentContainer(documentSysteManager:Sprite) {
    _documentSysteManager = documentSysteManager;
  }

  override public function addToSuperview(displayObjectContainer:DisplayObjectContainer, laf:LookAndFeel, superview:ContentView = null):void {
    super.addToSuperview(displayObjectContainer, laf, superview);

    _documentSysteManager.x = 16;
    _documentSysteManager.y = 16;
    addChild(_documentSysteManager);
    DocumentDisplayManager(_documentSysteManager).added();
  }

  override protected function draw(w:int, h:int):void {
    var documentSize:Rectangle = DocumentDisplayManager(_documentSysteManager).explicitDocumentSize;
    const padding:Number = 15 + 1;
    const totalPadding:Number = padding * 2;
    var actualDocumentWidth:Number = isNaN(documentSize.width) ? w - totalPadding : documentSize.width;
    var actualDocumentHeight:Number = isNaN(documentSize.height) ? h - totalPadding : documentSize.height;
    DocumentDisplayManager(_documentSysteManager).setActualDocumentSize(actualDocumentWidth, actualDocumentHeight);

    var g:Graphics = graphics;
    g.clear();
    g.beginFill(0xcbcbcb);
    g.drawRect(0, 0, w, h);
    g.endFill();

    drawDocumentBackground(_documentSysteManager.graphics, actualDocumentWidth, actualDocumentHeight);
  }

  private static function drawDocumentBackground(g:Graphics, w:Number, h:Number):void {
    g.clear();
    g.lineStyle(1, 0x999999); // intellij idea 0x515151, but it looks bad
    g.beginBitmapFill(gridBitmapData);
    g.drawRect(-1, -1, w + 1, h + 1);
    g.endFill();
  }
}
}