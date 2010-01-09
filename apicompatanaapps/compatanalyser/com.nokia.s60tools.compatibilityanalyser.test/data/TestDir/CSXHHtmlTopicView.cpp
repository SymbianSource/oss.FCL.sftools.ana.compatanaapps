/* ===========================================================================
*  Name        : CSXHHtmlTopicView.cpp
*  Part of     : Csxhelp Application
*  Interface   : Private
*  Description : CCSXHHtmlTopicView class definition
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
// INCLUDES
#include "CSXHHtmlTopicView.h"
#include "CSXHHtmlTopicContainer.h"
#include "CSXHHtmlTOC2.h"
#include "CSXHAppUi.h"
#include "CSXHDocument.h"
#include <CsHelp.rsg>
#include "CsHelp.hrh"

#include "CSXHViewIDs.h"
#include "CSXHKywdTOC1.h"
#include "CSXHHelpDataBase.h"
#include "CSXHContextTopic.h"
#include "CSXHHtmlTOC2.h"
#include "CSXHHtmlTOC1.h" 

#include <avkon.hrh>
#include <aknnotewrappers.h> 
#include <BrCtldefs.h>
#include <f32file.h>
#include <apgcli.h>
#include <akntitle.h>

#include <AiwMenu.h>
#include <AiwCommon.h>
#include <AiwServiceHandler.h>

TInt CCSXHHtmlTopicView::iFontSize = TBrCtlDefs::EFontSizeLevelNormal;

CCSXHHtmlTopicView* CCSXHHtmlTopicView::NewL(const TUid& aUid, const TInt& aFlags,const 
TRect& aRect)
    {
    CCSXHHtmlTopicView* temp = new(ELeave) CCSXHHtmlTopicView(aUid,aFlags);
    CleanupStack::PushL(temp);
    temp->ConstructL(aRect);
    CleanupStack::Pop(temp);
    return temp;
    }

CCSXHHtmlTopicView::CCSXHHtmlTopicView(const TUid& aUid, const TInt& aFlags):
                        iUid(aUid),iAppFlags(aFlags)
    {   
    }
CCSXHHtmlTopicView::~CCSXHHtmlTopicView()
    {
    if(iServiceHandler)
   		 delete iServiceHandler;
    
    if(iBCContainer)
        {
        delete iBCContainer;
        iBCContainer = NULL;
        }
    }
    
void CCSXHHtmlTopicView::ConstructL(const TRect& /*aRect*/)
    {
    BaseConstructL(iAppFlags);
    
    iServiceHandler = CAiwServiceHandler::NewL();
    iServiceHandler->AttachMenuL(R_CSXH_MENU_HTMLTOPICVIEW, R_AIWHELPAPP_INTEREST);
    }

void CCSXHHtmlTopicView::SetViewTypeL(TInt type)
	{
	if(type == iContextTopicView)
		return;
	
	if(type == EContextHtmlView)
		{
		iContextTopicView = ETrue;
		SetSoftKeysL(R_CSHELP_SOFTKEYS_OPTIONS_CLOSE_OPENLINK);
		}
	else
		{
		iContextTopicView = EFalse;
		SetSoftKeysL(R_CSHELP_SOFTKEYS_OPTIONS_BACK_OPENLINK);
		}	
	}
	
TInt CCSXHHtmlTopicView::GetViewType()	
	{
	return iContextTopicView ? EContextHtmlView : EHtmlView;
	}
    
TUid CCSXHHtmlTopicView::Id() const 
    {
    return iUid;
    }
// --------------------------------------------------------------------------
// Launches an application based on the Application UID and a View ID
// --------------------------------------------------------------------------    
void CCSXHHtmlTopicView::HandleCommandL(TInt  aCommand  )
    {
    switch(aCommand)
        {
        case EAknSoftkeyBack:
            {
            if(iBCContainer->HandleBackKeyL())
                return;
            CCSXHDocument *doc = static_cast<CCSXHDocument*>(AppUi()->Document());              
            CCSXHHtmlTOC2 *displayTopic = STATIC_CAST(CCSXHHtmlTOC2*,doc->GetDisplayTopic());
            CCSXHKywdTOC1* kywdParent = displayTopic->GetKywdParent();
            if(kywdParent)
                {
                doc->SetDisplayAndPrevTopic(kywdParent);    
                AppUi()->HandleCommandL(ECSXHOpenItem); 
                }
            else
                {
                HandleCommandL(ECSXHOpenApplicationTopics); 
                }   
            break;  
            }
        case ECSXHOpenApplicationTopics:       
            {
            CCSXHDocument *doc = static_cast<CCSXHDocument*>(AppUi()->Document());   
            if(iBCContainer->GetCurrActiveObject())
            	{
            	doc->SetDisplayTopic(iBCContainer->GetCurrActiveObject());
            	CCSXHAppUi::GetInstance()->ResetTOC2ViewContainer();
            	}            	           
            CCSXHHtmlTOC2 *displayTopic = STATIC_CAST(CCSXHHtmlTOC2*,doc->GetDisplayTopic());
            doc->SetDisplayAndPrevTopic(displayTopic->GetParent()); 
            AppUi()->HandleCommandL(ECSXHOpenItem);
            }
            break;
        case ECSXHTOC1ListView:
            {
            CCSXHDocument *doc = static_cast<CCSXHDocument*>(AppUi()->Document());      
            if(iBCContainer->GetCurrActiveObject())
            	{
            	doc->SetDisplayTopic(iBCContainer->GetCurrActiveObject());
            	CCSXHAppUi::GetInstance()->ResetTOC2ViewContainer();
            	}            	        
            	
            CCSXHHtmlTOC2 *displayTopic = STATIC_CAST(CCSXHHtmlTOC2*,doc->GetDisplayTopic());
            //These two steps are necessary to set the correct display and prev topics
            doc->SetDisplayTopic(displayTopic->GetParent());    
            doc->SetDisplayAndPrevTopic(doc->GetHelpDataBase()->GetMainTopics());   
            AppUi()->HandleCommandL(ECSXHOpenItem); 
            }
            break;
        case ECsHelpCmdFontLarge:
            {
            TInt CurrentFontSize = iBCContainer->GetCurrentValueL(TBrCtlDefs::ESettingsFontSize);
            iBCContainer->SetSelectedFontSizeL(++CurrentFontSize);
            iFontSize = CurrentFontSize;
            }
            break;
        case ECsHelpCmdFontSmall:
            {
            TInt CurrentFontSize = iBCContainer->GetCurrentValueL(TBrCtlDefs::ESettingsFontSize);
            iBCContainer->SetSelectedFontSizeL(--CurrentFontSize);
            iFontSize = CurrentFontSize;
            }
            break;
        case ECSXHOpenHyperLink: 
            {           
            iBCContainer->HandleCommandOpenHyperLinkL();
            }
            break;
		case ECSXHSearchText:
			{		
        	CCSXHDocument *doc = static_cast<CCSXHDocument*>(AppUi()->Document()); 
        	if(iBCContainer->GetCurrActiveObject())
	        	{
	        	doc->SetDisplayTopic(iBCContainer->GetCurrActiveObject());
	       		CCSXHAppUi::GetInstance()->ResetTOC2ViewContainer();
	        	}            
        	}
        case EAknSoftkeyExit:
        case EAknSoftkeyClose:
            AppUi()->HandleCommandL( aCommand );
            break;
        default:
            
            {
            CCSXHDocument *doc = static_cast<CCSXHDocument*>(AppUi()->Document());              
            CCSXHHtmlTOC2 *displayTopic = STATIC_CAST(CCSXHHtmlTOC2*,doc->GetDisplayTopic());
            TCoeHelpContext helpContext;
            displayTopic->GetHelpContext(helpContext);
		    CAiwGenericParamList* list = NewParamListLC(helpContext);		       	    		       	    
		    iServiceHandler->ExecuteMenuCmdL(aCommand,*list,iServiceHandler->OutParamListL());
		    CleanupStack::PopAndDestroy(list);
            }
            break;      
        }     
    }
// --------------------------------------------------------------------------
// Handlign for view activation. For a context view, the actual display topic
// has to be modified
// --------------------------------------------------------------------------
void CCSXHHtmlTopicView::DoActivateL(const TVwsViewId& aPrevViewId,TUid,const TDesC8&)  
    {
    iPrevious = aPrevViewId;    
    CCSXHDocument *doc = static_cast<CCSXHDocument*>(AppUi()->Document());  
     
    if(!iBCContainer)
    	{
    	TRAPD(Result,iBCContainer = CCSXHHtmlTopicContainer::NewL(ClientRect(),*doc,this));
    	if(KErrNone != Result)
    		return;
    	}
    
    //Set the correct display topic here for context sensitive launch
    if(doc->GetDisplayTopic()->GetViewID() == KCSXHContextHtmlTopicViewID)
        {
        CCSXHContextTopic *topic = STATIC_CAST(CCSXHContextTopic*,doc->GetDisplayTopic());
        doc->SetDisplayTopic(topic->GetTopic());
        iContextTopicView = ETrue;
        SetSoftKeysL(R_CSHELP_SOFTKEYS_OPTIONS_CLOSE_OPENLINK);
        }
    else
    	{
    	SetSoftKeysL(R_CSHELP_SOFTKEYS_OPTIONS_BACK_OPENLINK);
    	}    
	
	CCSXHHtmlTOC2 *displayTopic = STATIC_CAST(CCSXHHtmlTOC2*,doc->GetDisplayTopic());
    iBCContainer->SetAndLoadInitialTopicL(displayTopic);    
    iBCContainer->SetMopParent(this); 
    iBCContainer->SetRect(ClientRect());
    AppUi()->AddToStackL(*this, iBCContainer);    
    iBCContainer->MakeVisible(ETrue);     
    }
                             
void CCSXHHtmlTopicView::DoDeactivate()                             
    {   
    if(iBCContainer)
        {
        iBCContainer->MakeVisible(EFalse);
        AppUi()->RemoveFromStack(iBCContainer);
        }
    }
// --------------------------------------------------------------------------
// When a view which was previously activated, goes to background and gets
// activated again, the DoActivateL will not be called only ViewActivatedL 
// will be called. Special handling is required for the context view for the
// scenario, open context help from any application, with out changing the 
// help view, open context help from another application, the contents of the
// context view has to be refreshed with the new topic
// --------------------------------------------------------------------------    
void CCSXHHtmlTopicView::ViewActivatedL(const TVwsViewId& aPrevViewId,
                                 TUid aCustomMessageId,
                                 const TDesC8& aCustomMessage)
    {
    
    if(iBCContainer)
    	{
        //This handling is required for the following scenario
    	//Context sensitive help launch more than once with no 
	    //other view navigation in the help application
    	CCSXHDocument *doc = static_cast<CCSXHDocument*>(AppUi()->Document());
     	if(doc->GetDisplayTopic()->GetViewID() == KCSXHContextHtmlTopicViewID)
 	    	{
        	CCSXHContextTopic *topic = static_cast<CCSXHContextTopic*>(doc->GetDisplayTopic());
        	doc->SetDisplayTopic(topic->GetTopic());
        	CCSXHHtmlTOC2 *displayTopic = STATIC_CAST(CCSXHHtmlTOC2*,doc->GetDisplayTopic());
        	iBCContainer->RefreshL(displayTopic);
        	SetSoftKeysL(R_CSHELP_SOFTKEYS_OPTIONS_CLOSE_OPENLINK);
         	}
        iBCContainer->CheckForMSK();
    	}
    CAknView::ViewActivatedL(aPrevViewId,aCustomMessageId,aCustomMessage);
    }
    
void CCSXHHtmlTopicView::SetSoftKeysL(TInt aSoftKeys)
    {    
    Cba()->SetCommandSetL( aSoftKeys );
    Cba()->DrawDeferred();
    }
// --------------------------------------------------------------------------
// Dynamically updating Options menu item
// --------------------------------------------------------------------------    
void CCSXHHtmlTopicView::DynInitMenuPaneL(TInt aResourceId, CEikMenuPane* aMenuPane)
    {
    if(iServiceHandler->HandleSubmenuL(*aMenuPane))
        {
        	return;
        }
    
    if( R_CSXH_MENU_HTMLTOPICVIEW == aResourceId)
        {    
        TInt CurrentFontSize = iBCContainer->GetCurrentValueL(TBrCtlDefs::ESettingsFontSize);
        
        if(CurrentFontSize == TBrCtlDefs::EFontSizeLevelAllSmall)
            {
            aMenuPane->SetItemDimmed(ECsHelpCmdFontSmall,ETrue);
            }
        else if(CurrentFontSize == TBrCtlDefs::EFontSizeLevelAllLarge)
            {
            aMenuPane->SetItemDimmed(ECsHelpCmdFontLarge,ETrue);
            }
        
        if(!iBCContainer->IsHyperLinkFocused())
            {
            aMenuPane->SetItemDimmed(ECSXHOpenHyperLink,ETrue);
            }
     
        CCSXHDocument *doc = static_cast<CCSXHDocument*>(AppUi()->Document());              
        CCSXHHtmlTOC2 *displayTopic = STATIC_CAST(CCSXHHtmlTOC2*,doc->GetDisplayTopic());
        TCoeHelpContext helpContext;
        displayTopic->GetHelpContext(helpContext);
		CAiwGenericParamList* list = NewParamListLC(helpContext);		       	    		       	    
		iServiceHandler->InitializeMenuPaneL(*aMenuPane,aResourceId, ECmdLast, *list);
		CleanupStack::PopAndDestroy(list);
        }
    }
// --------------------------------------------------------------------------
// MSK Handling
// --------------------------------------------------------------------------
void CCSXHHtmlTopicView::SetMiddleSoftKey(TBool aValue)
    {
#ifndef __SERIES60_30__    
    if(AknLayoutUtils::MSKEnabled())
        {        
        Cba()->MakeCommandVisible(ECSXHOpenHyperLink,aValue);
        Cba()->DrawDeferred();
        }
#endif      
    }
    
TInt CCSXHHtmlTopicView::GetCurrentFontSize()
 	{
 	return iFontSize;
 	}
 	
void CCSXHHtmlTopicView::ResourceChangeHdl(TInt aType)
    {
    if(iBCContainer)
        iBCContainer->HandleResourceChangeImpl(aType);
    } 
    
CAiwGenericParamList* CCSXHHtmlTopicView::NewParamListLC(const TCoeHelpContext& aContext)
{
	TAiwVariant variant1;
	TAiwVariant variant2; 		
	variant1.Set(aContext.iMajor); 		
	TAiwGenericParam param1(EGenericParamHelpItem, variant1); 		
	variant2.Set(aContext.iContext); 		
	TAiwGenericParam param2(EGenericParamHelpItem, variant2); 	
	CAiwGenericParamList* list = CAiwGenericParamList::NewLC(); 		
	list->AppendL(param1);
	list->AppendL(param2); 	
	return list;
}	
 
    
