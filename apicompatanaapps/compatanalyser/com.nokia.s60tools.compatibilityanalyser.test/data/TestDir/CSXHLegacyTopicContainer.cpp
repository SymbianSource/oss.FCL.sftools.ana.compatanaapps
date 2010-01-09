/* ===========================================================================
*  Name        : CSXHLegacyTopicContainer.cpp
*  Part of     : Csxhelp Application
*  Interface   : Private
*  Description : CCSXHLegacyTopicContainer class definition
*  Version     : 
*
*  Copyright © 2006 Nokia.  All rights reserved.
*  This material, including documentation and any related computer
*  programs, is protected by copyright controlled by Nokia.  All
*  rights are reserved.  Copying, including reproducing, storing,
*  adapting or translating, any or all of this material requires the
*  prior written consent of Nokia.  This material also contains
*  confidential information which may not be disclosed to others
*  without the prior written consent of Nokia.
* ============================================================================
* Template version: 4.0
*/

#include "CSXHLegacyTOC2.h"
#include "CSXHLegacyTopicContainer.h"
#include "CSXHDocument.h"

#include "CSXHAppUi.h"

#include <eikrted.h>
#include <txtrich.h>
#include <aknkeys.h>
#include <aknutils.h>
#include <barsread.h>
#include <AppLayout.cdl.h>    //Dynamic layout functions
#include <aknsdrawutils.h> 
#include <AknsBasicBackgroundControlContext.h>
#include <AknLayoutScalable_Apps.cdl.h>
#include <AknAppUi.h>
#include <txtfrmat.h> 

CCSXHLegacyTopicContainer* CCSXHLegacyTopicContainer::NewL(const TRect& 
aRect,CCSXHDocument &aDocument,CCSXHLegacyTOC2 *aTopic)
    {
    CCSXHLegacyTopicContainer* self = CCSXHLegacyTopicContainer::NewLC(aRect,aDocument,aTopic);
    CleanupStack::Pop(self);
    return self;
    }
    
CCSXHLegacyTopicContainer* CCSXHLegacyTopicContainer::NewLC(const TRect& 
aRect,CCSXHDocument &aDocument,CCSXHLegacyTOC2 *aTopic)
    {
    CCSXHLegacyTopicContainer* self = new (ELeave) CCSXHLegacyTopicContainer(aDocument,aTopic);
    CleanupStack::PushL(self);
    self->ConstructL(aRect);
    return self;
    }

CCSXHLegacyTopicContainer::CCSXHLegacyTopicContainer(CCSXHDocument 
&aDocument,CCSXHLegacyTOC2 *aTopic):iDocument(aDocument),iTopic(aTopic)
    {
    }

void CCSXHLegacyTopicContainer::ConstructL(const TRect& aRect/*, CRichText* aText*/)
    {
    CreateWindowL();
    TRect rect(0,0,0,0);
    // Temporary rect is passed. Correct rect is set in SizeChanged.
    iSkinContext = CAknsBasicBackgroundControlContext::NewL(
       KAknsIIDQsnBgAreaMain , rect, EFalse);
       
    iText = STATIC_CAST(CRichText*,iTopic->GetTopicContentL());
    
    GetTextFormat();

    iEdwin = new(ELeave) CEikEdwin();
    const TInt flags(CEikEdwin::EKeepDocument |
                     CEikEdwin::EUserSuppliedText |
                     CEikEdwin::ENoAutoSelection |
                     CEikEdwin::EAvkonDisableCursor |
                     CEikEdwin::EReadOnly |
                     CEikEdwin::EDisplayOnly);
    iEdwin->SetContainerWindowL(*this);

    iEdwin->ConstructL(flags);
    iEdwin->CreatePreAllocatedScrollBarFrameL()->SetScrollBarVisibilityL(
                                                 CEikScrollBarFrame::EOff,
                                                 CEikScrollBarFrame::EAuto);
    SetTextL(iText);
    FormatRichTextL(*iText);
    SetRect(aRect);
    ActivateL();
    }

CCSXHLegacyTopicContainer::~CCSXHLegacyTopicContainer()
    {
    delete iEdwin;
    delete iSkinContext;
    }

void CCSXHLegacyTopicContainer::RefreshL(CCSXHLegacyTOC2 *aTopic)
    {
    iTopic = aTopic;
    iText = STATIC_CAST(CRichText*,iTopic->GetTopicContentL());
    SetTextL(iText);
    FormatRichTextL(*iText);
    SizeChanged();
    }

void CCSXHLegacyTopicContainer::SetTextL(CRichText* aText)
    {
    iEdwin->SetDocumentContentL(*aText, CEikEdwin::EUseText);
    iEdwin->SetCursorPosL(0, EFalse);
    
    }

void CCSXHLegacyTopicContainer::FocusChanged(TDrawNow aDrawNow)
    {
    if (iEdwin)
        {
        iEdwin->SetFocus(IsFocused(), aDrawNow);
        }
    }

void CCSXHLegacyTopicContainer::GetTextFormat()
    {
    
    TAknMultiLineTextLayout layoutToUse;
    if( AknLayoutUtils::ScalableLayoutInterfaceAvailable() )
        {
        
        TAknTextLineLayout layoutnew1 = AknLayoutScalable_Apps::help_list_pane_t1(0).LayoutLine();
        

        TAknLayoutScalableParameterLimits limits1 = 
            AknLayoutScalable_Apps::help_list_pane_t1_ParamLimits();
            
        TInt numberoflines = limits1.LastRow() + 1;
        
        layoutnew1.iNumberOfLinesShown = numberoflines ;
    
        layoutToUse.iC = layoutnew1.iC;
        layoutToUse.il = layoutnew1.il;
        layoutToUse.ir = layoutnew1.ir;
        layoutToUse.iB = layoutnew1.iB;
        layoutToUse.iW = layoutnew1.iW;
        layoutToUse.iJ = layoutnew1.iJ;
        layoutToUse.iFont = layoutnew1.iFont;
        layoutToUse.iBaselineSkip = layoutnew1.iBaselineSkip;
        layoutToUse.iNumberOfLinesShown = layoutnew1.iNumberOfLinesShown;
        }
    else
        {
        layoutToUse = AppLayout::Multiline_Help_texts_Line_1(0);
        }

    const CFont* font = AknLayoutUtils::FontFromId(layoutToUse.FontId() /*iFont*/, NULL);
    iCharFormat.iFontSpec = font->FontSpecInTwips();
    iCharFormatMask.SetAttrib(EAttFontTypeface);
    iCharFormatMask.SetAttrib(EAttFontHeight);

    TRgb color;
    MAknsSkinInstance* skin = AknsUtils::SkinInstance();
    TInt error = AknsUtils::GetCachedColor(skin, color, KAknsIIDQsnTextColors, 
    													EAknsCIQsnTextColorsCG6 );
    if(error==KErrNone)
        iCharFormat.iFontPresentation.iTextColor = color;
    
    iCharFormatMask.SetAttrib(EAttColor);
    iCharFormatMask.SetAttrib(EAttFontPosture);    
 }
void CCSXHLegacyTopicContainer::FormatRichTextL( CRichText& aText )
    {
    TInt length = aText.DocumentLength();
    length++;
    // Apply the general formatting rules...
    aText.ApplyCharFormatL( iCharFormat, iCharFormatMask, 0, length );
    
    TParaFormatMask paraFormatMask;
	paraFormatMask.SetAttrib( EAttBullet );
	CParaFormat *paraFormat  = new ( ELeave ) CParaFormat;
	CleanupStack::PushL(paraFormat);	
	TInt paraCount = aText.ParagraphCount();
	for(TInt i = 0; i < paraCount ; ++i)
		{
		TInt paraLength;
		TInt paraStart = aText.CharPosOfParagraph(paraLength,i);
		
		aText.GetParagraphFormatL(paraFormat,paraStart);
		if(paraFormat->iBullet)
			{
			TBullet* bullet = new ( ELeave ) TBullet;
			CleanupStack::PushL(bullet);
			bullet->iColor = iCharFormat.iFontPresentation.iTextColor;
			bullet->iHeightInTwips = iCharFormat.iFontSpec.iHeight;
			
			CParaFormat* paraFormatNew = new ( ELeave ) CParaFormat;			
			paraFormatNew->iBullet = bullet; // ownership xfer
			
			CleanupStack::Pop();
			CleanupStack::PushL(paraFormatNew);
			
			aText.ApplyParaFormatL( paraFormatNew, paraFormatMask, paraStart, paraLength - 1);
			CleanupStack::PopAndDestroy(paraFormatNew);
			}
		}
	CleanupStack::PopAndDestroy(paraFormat);
    }


void CCSXHLegacyTopicContainer::SizeChanged()
    {
	if(iSkinContext)
        	iSkinContext->SetRect(Rect());
    if(iEdwin)
    	{
    	TRect clientrect = Rect();
    	if(AknLayoutUtils::LayoutMirrored())
			{
			clientrect.iTl.iX = clientrect.iTl.iX + (iEdwin->ScrollBarFrame()
    				->ScrollBarBreadth(CEikScrollBar::EVertical))* 1.1;    		
			}
		else
			{
    		TInt RectWidth =  clientrect.Width() - (iEdwin->ScrollBarFrame()
    				->ScrollBarBreadth(CEikScrollBar::EVertical))* 1.1;      
			
    		clientrect.SetWidth(RectWidth);
			}
    	
       	iEdwin->SetRect(clientrect);
       	TRAP_IGNORE(iEdwin->ForceScrollBarUpdateL());
    	}    
    }

// ---------------------------------------------------------
// CCsHelpTopicContainer::OfferKeyEventL(...)
// Processing a key event
//  
// (other items were commented in a header).
// ---------------------------------------------------------
TKeyResponse CCSXHLegacyTopicContainer::OfferKeyEventL(
                                      const TKeyEvent& aKeyEvent,
                                      TEventCode aType)
    {
    TKeyResponse result(EKeyWasConsumed);
 
    switch (aKeyEvent.iCode)
        {            
        case EKeyUpArrow:
            iEdwin->MoveDisplayL(TCursorPosition::EFLineUp);
            iEdwin->UpdateScrollBarsL();
            break;
        case EKeyDownArrow:
            iEdwin->MoveDisplayL(TCursorPosition::EFLineDown);
            iEdwin->UpdateScrollBarsL();
            break;
        default:
            result = iEdwin->OfferKeyEventL(aKeyEvent, aType);
        }

    return result;
    }


// ---------------------------------------------------------
// CCsHelpTopicContainer::CountComponentControls() const
//
//  
// (other items were commented in a header).
// ---------------------------------------------------------
//
TInt CCSXHLegacyTopicContainer::CountComponentControls() const
    {
    return 1; // return nbr of controls inside this container
    }

// ---------------------------------------------------------
// CCsHelpTopicContainer::ComponentControl(TInt aIndex) const
//
// (other items were commented in a header).
// ---------------------------------------------------------
//
CCoeControl* CCSXHLegacyTopicContainer::ComponentControl(TInt aIndex) const
    {
    switch ( aIndex )
        {
        case 0:
            return iEdwin;
        default:
            return NULL;
        }
    }

// ---------------------------------------------------------
// CCsHelpTopicContainer::Draw(..)
//
// (other items were commented in a header).
// ---------------------------------------------------------
//
void CCSXHLegacyTopicContainer::Draw(const TRect& aRect) const
    {
    CWindowGc& gc = SystemGc();
    gc.Clear(aRect);
    MAknsSkinInstance* skin = AknsUtils::SkinInstance();
    if (iSkinContext)
        {//Draw the skin background
        AknsDrawUtils::Background(
            skin, iSkinContext, this, gc, aRect);
        }
    }
void CCSXHLegacyTopicContainer::HandleResourceChange(TInt aType)
    {
     CCSXHAppUi::GetInstance()->PropagateResourceChange(aType);                
    }

void CCSXHLegacyTopicContainer::HandleResourceChangeImpl(TInt aType)
    {
    if(aType == KEikDynamicLayoutVariantSwitch)
        {
        iEdwin->HandleResourceChange(aType);
        TRect mainRect; 
        AknLayoutUtils::LayoutMetricsRect(AknLayoutUtils::EMainPane,mainRect);        
        SetRect(mainRect);        
        }
    else
        {
        CCoeControl::HandleResourceChange(aType);
        }
    
    }


// ---------------------------------------------------------
// CCsHelpTopicContainer::MopSupplyObject()
// Pass skin information if need.
// ---------------------------------------------------------

TTypeUid::Ptr CCSXHLegacyTopicContainer::MopSupplyObject(TTypeUid aId)
    {
    if (aId.iUid == MAknsControlContext::ETypeId && iSkinContext)
        {
        return MAknsControlContext::SupplyMopObject(aId, iSkinContext);
        }

    return CCoeControl::MopSupplyObject(aId);
    }
// End of File  
