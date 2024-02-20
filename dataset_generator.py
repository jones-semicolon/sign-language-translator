# import cv2 as cv
# import os
# from time import sleep
# import numpy as np
#
# #argument 0 is given to use the default camera of the laptop
# camera = cv.VideoCapture(0)
# #Now check if the camera object is created successfully
# if not camera.isOpened():
#     print("The Camera is not Opened....Exiting")
#     exit()
#
#
# Labels = []
# with open("labels.txt", "r") as f:
#     for line in f:
#         Labels.append(line.strip())
#
# print(Labels)
#
# for folder in Labels:
#     #using count variable to name the images in the dataset.
#     while True:
#         count = 0
#         #Taking input to start the capturing
#         # print("Press 's' to start data collection for "+folder)
#         # userinput = input()
#         # if userinput != 's':
#         #     print("Wrong Input..........")
#         #     exit()
#         #clicking 200 images per label, you could change as you want.
#         #read returns two values one is the exit code and other is the frame
#         status, frame = camera.read()
#         #check if we get the frame or not
#         if not status:
#             print("Frame is not been captured..Exiting...")
#             break
#         #convert the image into gray format for fast caculation
#         gray = cv.cvtColor(frame, cv.COLOR_BGR2GRAY)
#         #display window with gray image
#         cv.imshow("Video Window",gray)
#         #resizing the image to store it
#         gray = cv.resize(gray, (200,200))
#         #to quite the display window press 'q'
#         key = cv.waitKey(1)
#         if key == ord('q'):
#             camera.release()
#             cv.destroyAllWindows()
#             break
#         if key == ord('s'):
#             print('get ready to capture '+ folder +' in 3secs')
#             sleep(3)
#             while count<200:
#             #Store the image to specific label folder
#                 folder_path = 'slt_dataset/' + folder
#                 if not os.path.exists(folder_path):
#                     os.mkdir(folder_path)
#                 cv.imwrite(folder_path + '/' + folder + '_' + str(count) + '.jpg', gray)
#                 # sleep(1)
#                 count=count+1
#             print(folder+' is now saved to ' + folder_path + ' directory')
#             break
#
# # When everything done, release the capture
# camera.release()
# cv.destroyAllWindows()

import cv2 as cv
import os
from time import sleep

# Argument 0 is given to use the default camera of the laptop
camera = cv.VideoCapture(0)
# Now check if the camera object is created successfully
if not camera.isOpened():
    print("The Camera is not Opened....Exiting")
    exit()

Labels = []
with open("labels.txt", "r") as f:
    for line in f:
        Labels.append(line.strip())

print(Labels)

for folder in Labels:
    # Create the folder if it doesn't exist
    folder_path = 'slt_dataset/' + folder
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)

    print('Collecting data for ' + folder)
    sleep(3)

    count = 0
    capturing = False
    while count < 200:
        # Read frame from camera
        status, frame = camera.read()
        # Check if frame is captured successfully
        if not status:
            print("Frame is not been captured..Exiting...")
            break
        # Convert the image into gray format for fast calculation
        # gray = cv.cvtColor(frame, cv.COLOR_BGR2GRAY)
        # Display window with gray image
        cv.imshow("Video Window", frame)
        image = cv.resize(frame ,(200,200))

        # To quit the display window press 'q'
        key = cv.waitKey(1)
        if key == ord('q'):
            camera.release()
            cv.destroyAllWindows()
            break
        elif key == ord('s'):
            capturing = True
            print('Capturing frames for ' + folder)
        elif capturing:
            # Store the image to specific label folder
            cv.imwrite(folder_path + '/' + folder + '_' + str(count) + '.jpg', image)
            print('Image ' + str(count + 1) + ' captured for ' + folder)
            count += 1

# When everything done, release the capture
camera.release()
cv.destroyAllWindows()
