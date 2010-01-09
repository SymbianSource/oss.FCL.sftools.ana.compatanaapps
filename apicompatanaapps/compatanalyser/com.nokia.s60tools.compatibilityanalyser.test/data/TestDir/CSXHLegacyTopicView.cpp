/* ===========================================================================
*  Name        : CSXHLegacyTopicView.cpp
*  Part of     : Csxhelp Application
*  Interface   : Private
*  Description : CCSXHLegacyTopicView class definition
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
#include "CSXHLegacyTopicView.h"
#include "CSXHLegacyTopicContainer.h"
#include "CSXHAppUi.h"
#include "CSXHDocument.h"
#include <CsHelp.rsg>
#include "CsHelp.hrh"

#include "CSXHHelpContentBase.h"
#include "CSXHLegacyTOC2.h"
#include "CSXHLegacyTOC1.h" 
#include "CSXHViewIDs.h"
#include "CSXHHelpDataBase.h"
#include "CSXHContextTopic.h"

#include <akntitle.h>

CCSXHLegacyTopicView* CCSXHLegacyTopicView::NewL(const TUid& aUid, 
                                        const TInt& aFlags,const TRect& aRect)
    {
    CCSXHLegacyTopicView* temp = new(ELeave) CCSXHLegacyTopicView(aUid,aFlags);
    CleanupStack::PushL(temp);
    temp->ConstructL(aRect);
    CleanupStack::Pop(temp);
    return temp;
    }

CCSXHLegacyTopicView::CCSXHLegacyTopicView(const TUid& aUid, const TInt& aFlags)
                        : iUid(aUid),iAppFlags(aFlags)
    {
    }
    
CCSXHLegacyTopicView::~CCSXHLegacyTopicView()
    {
    }
    
void CCSXHLegacyTopicView::ConstructL(const TRect& /*aRect*/)
    {
    BaseConstructL(iAppFlags);
    }
    
TUid CCSXHLegacyTopicView::Id() const   
    {
    return iUid;
    }
// --------------------------------------------------------------------------
// Launches an application based on the Application UID and a View ID
// --------------------------------------------------------------------------
void CCSXHLegacyTopicView::HandleCommandL(TInt aCommand)
    {
    switch(aCommand)
        {       
        case ECSXHOpenApplicationTopics:
            {
            CCSXHDocument* doc = static_cast<CCSXHDocument*>(AppUi()->Document());              
            CCSXHLegacyTOC2* displayTopic = STATIC_CAST(CCSXHLegacyTOC2*,doc->GetDisplayTopic());
            CCSXHLegacyTOC1* toc1 = displayTopic->GetLegacyParent();
            if(toc1)
                {
                doc->SetDisplayAndPrevTopic(toc1);
                AppUi()->HandleCommandL(ECSXHOpenItem);
                }
            else
                {
                //Handling will be same as EAknSoftkeyBack
                HandleCommandL(EAknSoftkeyBack);
                }
            break;      
            }
        case EAknSoftkeyBack:
            {
            CCSXHDocument* doc = static_cast<CCSXHDocument*>(AppUi()->Document());              
            CCSXHLegacyTOC2* displayTopic = STATIC_CAST(CCSXHLegacyTOC2*,doc->GetDisplayTopic());
            doc->SetDisplayAndPrevTopic(displayTopic->GetParent());
            AppUi()->HandleCommandL(ECSXHOpenItem);                 
            }               
        break;
        case ECSXHTOC1ListView:
            {
            CCSXHDocument* doc = static_cast<CCSXHDocument*>(AppUi()->Document());              
            CCSXHLegacyTOC2* displayTopic = STATIC_CAST(CCSXHLegacyTOC2*,doc->GetDisplayTopic());
            CCSXHGenericTOC1* toc1 = displayTopic->GetLegacyParent();
            if(!toc1)
                toc1 = displayTopic->GetParent();
            doc->SetDisplayTopic(toc1);
            doc->SetDisplayAndPrevTopic(doc->GetHelpDataBase()->GetMainTopics());   
            AppUi()->HandleCommandL(ECSXHOpenItem);
            }
            break;  
        default:
            AppUi()->HandleCommandL(aCommand);  
            break;
        }   
    }

// --------------------------------------------------------------------------
// Handlign for view activation. For a context view, the actual display topic
// has to be modified
// --------------------------------------------------------------------------
void CCSXHLegacyTopicView::DoActivateL(const TVwsViewId& aPrevViewId,TUid /* aCustomMessageId */,
                             const TDesC8& /* aCustomMessage */)    
    {
    iPrevious = aPrevViewId;
    CCSXHDocument* doc = static_cast<CCSXHDocument*>(AppUi()->Document());
     //Set the correct display topic here for context sensitive launch
    if(iUid == KCSXHContextLegacyTopicViewID)
        {
        CCSXHContextTopic* topic = STATIC_CAST(CCSXHContextTopic*,doc->GetDisplayTopic());
        doc->SetDisplayTopic(topic->GetTopic());
        }
            
    if(!iTopicContainer)
        {
        CCSXHLegacyTOC2* displayTopic = STATIC_CAST(CCSXHLegacyTOC2*,doc->GetDisplayTopic());
        iTopicContainer = CCSXHLegacyTopicContainer::NewL(ClientRect(),*doc,displayTopic);
        }
        
    iTopicContainer->SetMopParent(this);
    iTopicContainer->SetRect(ClientRect());
    AppUi()->AddToStackL(*this, iTopicContainer);
    iTopicContainer->MakeVisible(ETrue);
    
    //Set the title of the view
    CEikStatusPane* sp = StatusPane();
    CAknTitlePane* title = STATIC_CAST(CAknTitlePane*, 
            sp->ControlL(TUid::Uid(EEikStatusPaneUidTitle)));
    title->SetTextL(doc->GetDisplayTopic()->GetName());
    
   	//MSK: Should be Inactive in this view
	//TSW Error:EAJA-6XLFTW :
	//S60 3.2 Help: Selection key opens Option menu in Topic view 
	//If MSK is not defined, "." appears in the MSK, 
	//On Middle key press, Options menu items are shown to the user 
#ifndef __SERIES60_30__    
    if(AknLayoutUtils::MSKEnabled())
        {        
        Cba()->MakeCommandVisible(ECSXHDummyMSK,EFalse);
        Cba()->DrawDeferred();
        }
#endif 
    }
                             
void CCSXHLegacyTopicView::DoDeactivate()                             
    {
    if(iTopicContainer)
        {
        iTopicContainer->MakeVisible(EFalse);
        AppUi()->RemoveFromStack(iTopicContainer);
        delete iTopicContainer;
        iTopicContainer = NULL;
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
void CCSXHLegacyTopicView::ViewActivatedL(const TVwsViewId& aPrevViewId,
                                 TUid aCustomMessageId,
                                 const TDesC8& aCustomMessage)
    {
    if(iTopicContainer && iUid == KCSXHContextLegacyTopicViewID)
        {
        CCSXHDocument* doc = static_cast<CCSXHDocument*>(AppUi()->Document());
        
        if(doc->GetDisplayTopic()->GetViewID() == KCSXHContextLegacyTopicViewID)
            {
            CCSXHContextTopic* topic = STATIC_CAST(CCSXHContextTopic*,doc->GetDisplayTopic());
            doc->SetDisplayTopic(topic->GetTopic());
            CCSXHLegacyTOC2* displayTopic = STATIC_CAST(CCSXHLegacyTOC2*,doc->GetDisplayTopic());
            iTopicContainer->RefreshL(displayTopic);    
            }
        
         //Set the title of the view
        CEikStatusPane* sp = StatusPane();
        CAknTitlePane* title = STATIC_CAST(CAknTitlePane*, 
            sp->ControlL(TUid::Uid(EEikStatusPaneUidTitle)));
        title->SetTextL(doc->GetDisplayTopic()->GetName());
        }
        
    CAknView::ViewActivatedL(aPrevViewId,aCustomMessageId,aCustomMessage);
    }
void CCSXHLegacyTopicView::ViewDeactivated()
    {
    CAknView::ViewDeactivated();
    }
void CCSXHLegacyTopicView::ResourceChangeHdl(TInt aType)
    {
    if(iTopicContainer)
        iTopicContainer->HandleResourceChangeImpl(aType);
    }
    
